package com.example.play

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlayUpdateState {
    data object Idle : PlayUpdateState()
    data class UpdateAvailable(
        val updateInfo: AppUpdateInfo,
        val availableVersionCode: Int
    ) : PlayUpdateState()
    data class Downloading(val progressPercent: Int) : PlayUpdateState()
    data object DownloadedReadyToInstall : PlayUpdateState()
}

class PlayStoreManager(context: Context) {

    private val appContext = context.applicationContext
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(appContext)

    private val _updateState = MutableStateFlow<PlayUpdateState>(PlayUpdateState.Idle)
    val updateState: StateFlow<PlayUpdateState> = _updateState.asStateFlow()

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val total = state.totalBytesToDownload()
                val downloaded = state.bytesDownloaded()
                val pct = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
                _updateState.value = PlayUpdateState.Downloading(pct)
            }
            InstallStatus.DOWNLOADED -> {
                _updateState.value = PlayUpdateState.DownloadedReadyToInstall
            }
            InstallStatus.INSTALLED, InstallStatus.CANCELED, InstallStatus.FAILED -> {
                _updateState.value = PlayUpdateState.Idle
            }
            else -> Unit
        }
    }

    fun registerListener() {
        runCatching { appUpdateManager.registerListener(installStateListener) }
    }

    fun unregisterListener() {
        runCatching { appUpdateManager.unregisterListener(installStateListener) }
    }

    /**
     * Automatically checks Google Play for an available update on app startup / resume.
     */
    fun checkForUpdatesSilently() {
        runCatching {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> {
                        _updateState.value = PlayUpdateState.DownloadedReadyToInstall
                    }
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ||
                            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) -> {
                        _updateState.value = PlayUpdateState.UpdateAvailable(
                            updateInfo = info,
                            availableVersionCode = info.availableVersionCode()
                        )
                    }
                }
            }
        }
    }

    /**
     * Triggered when the user taps "Check for Updates" in Settings or About.
     */
    fun checkForUpdatesManual(
        onUpToDate: () -> Unit,
        onUnavailableFallback: () -> Unit
    ) {
        runCatching {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    when {
                        info.installStatus() == InstallStatus.DOWNLOADED -> {
                            _updateState.value = PlayUpdateState.DownloadedReadyToInstall
                        }
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                            _updateState.value = PlayUpdateState.UpdateAvailable(
                                updateInfo = info,
                                availableVersionCode = info.availableVersionCode()
                            )
                        }
                        else -> onUpToDate()
                    }
                }
                .addOnFailureListener {
                    // Outside Play Store installation (e.g. sideload/emulator), open Play Store gracefully
                    onUnavailableFallback()
                }
        }.onFailure {
            onUnavailableFallback()
        }
    }

    fun startUpdateFlow(
        updateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFallbackOpenPlayStore: () -> Unit
    ) {
        val updateType = if (updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            AppUpdateType.FLEXIBLE
        } else {
            AppUpdateType.IMMEDIATE
        }

        val started = runCatching {
            appUpdateManager.startUpdateFlowForResult(
                updateInfo,
                launcher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        }.getOrDefault(false)

        if (!started) {
            onFallbackOpenPlayStore()
        }
    }

    fun dismissUpdatePrompt() {
        if (_updateState.value is PlayUpdateState.UpdateAvailable) {
            _updateState.value = PlayUpdateState.Idle
        }
    }

    fun completeDownloadedUpdate() {
        runCatching { appUpdateManager.completeUpdate() }
    }

    /**
     * Launches Google Play In-App Review sheet; falls back cleanly to opening Play Store if unavailable.
     */
    fun launchInAppReview(activity: Activity, onFinished: () -> Unit) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { onFinished() }
                } else {
                    openPlayStoreListing(activity)
                    onFinished()
                }
            }
    }

    fun openPlayStoreListing(context: Context) {
        val pkg = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(marketIntent)
        }.onFailure {
            openWebUrl(context, "https://play.google.com/store/apps/details?id=$pkg")
        }
    }

    fun openWebUrl(context: Context, url: String) {
        val intent = if (url.startsWith("mailto:", ignoreCase = true)) {
            Intent(Intent.ACTION_SENDTO, Uri.parse(url)).apply {
                putExtra(Intent.EXTRA_SUBJECT, "SnapTube Support")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        runCatching { context.startActivity(intent) }
    }
}
