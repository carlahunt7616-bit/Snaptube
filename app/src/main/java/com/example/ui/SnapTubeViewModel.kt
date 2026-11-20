package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.SnapTubeApplication
import com.example.analyzer.MediaLinkAnalyzer
import com.example.data.local.DownloadRepository
import com.example.data.local.DownloadTaskEntity
import com.example.data.local.SettingsRepository
import com.example.data.model.AnalyzedMedia
import com.example.data.model.AppLanguage
import com.example.data.model.FormatUtils
import com.example.data.model.MediaType
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import com.example.download.DownloadEngine
import com.example.download.EnqueueResult
import com.example.notification.NotificationHelper
import com.example.storage.MediaStorageManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    data object Idle : AnalysisUiState()
    data class Loading(val url: String) : AnalysisUiState()
    data class Ready(
        val media: AnalyzedMedia,
        val selectedOptionId: String?,
        val showMoreFormats: Boolean = false
    ) : AnalysisUiState()
    data class Error(val url: String, val message: String) : AnalysisUiState()
}

sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data object RequestNotificationPermission : UiEvent()
    data class SwitchTab(val tabIndex: Int) : UiEvent()
}

class SnapTubeViewModel(
    private val analyzer: MediaLinkAnalyzer,
    private val downloadEngine: DownloadEngine,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val storageManager: MediaStorageManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    val activeTasks: StateFlow<List<DownloadTaskEntity>> = downloadRepository.activeTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val completedTasks: StateFlow<List<DownloadTaskEntity>> = downloadRepository.completedTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _urlError = MutableStateFlow<String?>(null)
    val urlError: StateFlow<String?> = _urlError.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _showDownloadSheet = MutableStateFlow(false)
    val showDownloadSheet: StateFlow<Boolean> = _showDownloadSheet.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var lastHandledSharedUrl: String? = null
    private var lastHandledSharedTimestamp: Long = 0L

    fun onUrlInputChange(value: String) {
        _urlInput.value = value
        if (_urlError.value != null) _urlError.value = null
    }

    fun clearUrlInput() {
        _urlInput.value = ""
        _urlError.value = null
    }

    fun analyzeUrl(rawInput: String = _urlInput.value, openSheetImmediately: Boolean = true) {
        val extracted = FormatUtils.extractFirstHttpUrl(rawInput) ?: rawInput.trim()
        if (!FormatUtils.isValidHttpUrl(extracted)) {
            _urlError.value = "Please enter a valid http:// or https:// media URL."
            return
        }

        _urlInput.value = extracted
        _urlError.value = null
        if (openSheetImmediately) {
            _showDownloadSheet.value = true
        }
        _analysisState.value = AnalysisUiState.Loading(extracted)

        viewModelScope.launch {
            val result = analyzer.analyze(extracted)
            result.fold(
                onSuccess = { media ->
                    val currentSettings = settings.value
                    val defaultOption = pickDefaultOption(media, currentSettings)
                    _analysisState.value = AnalysisUiState.Ready(
                        media = media,
                        selectedOptionId = defaultOption?.id,
                        showMoreFormats = false
                    )
                },
                onFailure = { error ->
                    _analysisState.value = AnalysisUiState.Error(
                        url = extracted,
                        message = error.message ?: "Unable to analyze this media URL."
                    )
                }
            )
        }
    }

    fun handleSharedText(rawSharedText: String, showNotification: Boolean = true) {
        val extracted = FormatUtils.extractFirstHttpUrl(rawSharedText)
        if (extracted == null) {
            _events.tryEmit(UiEvent.ShowMessage("No valid media URL found in shared content."))
            return
        }

        val now = System.currentTimeMillis()
        if (extracted == lastHandledSharedUrl && (now - lastHandledSharedTimestamp) < 2500L) {
            return
        }
        lastHandledSharedUrl = extracted
        lastHandledSharedTimestamp = now

        if (showNotification && settings.value.notificationsEnabled) {
            notificationHelper.showShareDetectedNotification(extracted)
        } else {
            notificationHelper.cancelShareNotification()
        }

        analyzeUrl(extracted, openSheetImmediately = true)
    }

    fun selectOption(optionId: String) {
        val current = _analysisState.value as? AnalysisUiState.Ready ?: return
        _analysisState.value = current.copy(selectedOptionId = optionId)
    }

    fun toggleMoreFormats() {
        val current = _analysisState.value as? AnalysisUiState.Ready ?: return
        _analysisState.value = current.copy(showMoreFormats = !current.showMoreFormats)
    }

    fun dismissDownloadSheet() {
        _showDownloadSheet.value = false
        notificationHelper.cancelShareNotification()
    }

    fun startSelectedDownload() {
        val current = _analysisState.value as? AnalysisUiState.Ready ?: return
        val option = current.media.options.firstOrNull { it.id == current.selectedOptionId }
            ?: current.media.options.firstOrNull()
            ?: return

        viewModelScope.launch {
            if (!notificationHelper.hasNotificationPermission() && settings.value.notificationsEnabled) {
                _events.emit(UiEvent.RequestNotificationPermission)
            }

            when (downloadEngine.enqueueDownload(current.media, option)) {
                is EnqueueResult.Enqueued -> {
                    _showDownloadSheet.value = false
                    notificationHelper.cancelShareNotification()
                    _events.emit(UiEvent.ShowMessage("Added to download queue"))
                    _events.emit(UiEvent.SwitchTab(1))
                }
                EnqueueResult.DuplicateActive -> {
                    _events.emit(UiEvent.ShowMessage("This link is already active in your download queue."))
                }
                EnqueueResult.WifiRequired -> {
                    _events.emit(UiEvent.ShowMessage("Wi-Fi Only mode is enabled. Connect to Wi-Fi or adjust Settings."))
                }
            }
        }
    }

    fun pauseDownload(taskId: Long) = downloadEngine.pauseDownload(taskId)
    fun resumeDownload(taskId: Long) = downloadEngine.resumeDownload(taskId)
    fun retryDownload(taskId: Long) = downloadEngine.retryDownload(taskId)
    fun cancelDownload(taskId: Long) = downloadEngine.cancelDownload(taskId)

    fun renameCompletedFile(task: DownloadTaskEntity, newTitle: String) {
        val cleaned = newTitle.trim()
        if (cleaned.isBlank()) return
        viewModelScope.launch {
            val newFileName = FormatUtils.sanitizeFileName(cleaned, task.formatExt)
            val updatedUri = storageManager.renameFile(task.localUri, newFileName)
            downloadRepository.updateFileMetadata(task.id, cleaned, newFileName, updatedUri)
            _events.emit(UiEvent.ShowMessage("Renamed to $newFileName"))
        }
    }

    fun deleteCompletedFile(task: DownloadTaskEntity) {
        viewModelScope.launch {
            storageManager.deleteFile(task.localUri)
            downloadRepository.deleteById(task.id)
            _events.emit(UiEvent.ShowMessage("Deleted ${task.title}"))
        }
    }

    fun clearDownloadHistory() {
        viewModelScope.launch {
            downloadRepository.clearHistory()
            _events.emit(UiEvent.ShowMessage("Download history cleared"))
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setDefaultVideoQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setDefaultVideoQuality(quality) }
    }

    fun setDefaultAudioQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setDefaultAudioQuality(quality) }
    }

    fun setDownloadFolder(treeUri: Uri) {
        viewModelScope.launch {
            val label = storageManager.resolveSafFolderLabel(treeUri)
            settingsRepository.setDownloadFolder(treeUri.toString(), label)
            _events.emit(UiEvent.ShowMessage("Download folder set to $label"))
        }
    }

    fun resetDownloadFolder() {
        viewModelScope.launch {
            settingsRepository.setDownloadFolder(null, null)
            _events.emit(UiEvent.ShowMessage("Download folder reset to Downloads/SnapTube"))
        }
    }

    fun setMaxConcurrentDownloads(limit: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxConcurrentDownloads(limit)
            downloadEngine.onConcurrencyLimitChanged()
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnly(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            if (enabled && !notificationHelper.hasNotificationPermission()) {
                _events.emit(UiEvent.RequestNotificationPermission)
            }
        }
    }

    private fun pickDefaultOption(media: AnalyzedMedia, settings: UserSettings) =
        media.options.firstOrNull {
            it.mediaType == MediaType.VIDEO && it.qualityLabel.equals(settings.defaultVideoQuality, ignoreCase = true)
        } ?: media.options.firstOrNull {
            it.mediaType == MediaType.AUDIO && it.qualityLabel.equals(settings.defaultAudioQuality, ignoreCase = true)
        } ?: media.options.firstOrNull()

    class Factory(private val app: SnapTubeApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SnapTubeViewModel(
                analyzer = app.mediaLinkAnalyzer,
                downloadEngine = app.downloadEngine,
                downloadRepository = app.downloadRepository,
                settingsRepository = app.settingsRepository,
                storageManager = app.storageManager,
                notificationHelper = app.notificationHelper
            ) as T
        }
    }
}
