package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.AppLanguage
import com.example.data.model.LegalDocumentType
import com.example.play.PlayStoreManager
import com.example.play.PlayUpdateState
import com.example.ui.AnalysisUiState
import com.example.ui.SnapTubeViewModel
import com.example.ui.UiEvent
import com.example.ui.components.GradientPrimaryButton
import com.example.ui.components.SnapIcons
import com.example.ui.components.SnapTubeLogoMark
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CompletedLibraryScreen
import com.example.ui.screens.DownloadManagerScreen
import com.example.ui.screens.DownloadOptionsBottomSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalDocumentScreen
import com.example.ui.screens.MediaPlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BrandGoldPrimary
import com.example.ui.theme.SnapTubeTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: SnapTubeViewModel by viewModels {
        SnapTubeViewModel.Factory(application as SnapTubeApplication)
    }

    private lateinit var playStoreManager: PlayStoreManager

    private val inAppUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { /* Flexible/Immediate update result handled by InstallStateUpdatedListener */ }

    private var launchedFromExternalShare by mutableStateOf(false)
    private var requestedTabFromIntent by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        playStoreManager = PlayStoreManager(this).apply {
            registerListener()
            checkForUpdatesSilently()
        }

        handleIncomingIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val updateState by playStoreManager.updateState.collectAsStateWithLifecycle()
            val localizedContext = remember(settings.language) {
                createLocalizedContext(this, settings.language)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalOnBackPressedDispatcherOwner provides this@MainActivity
            ) {
                SnapTubeTheme(themeMode = settings.themeMode) {
                    SnapTubeRootApp(
                        viewModel = viewModel,
                        updateState = updateState,
                        skipSplash = launchedFromExternalShare,
                        externalRequestedTab = requestedTabFromIntent,
                        onConsumeExternalTab = { requestedTabFromIntent = null },
                        onOpenExternalFile = { task -> openCompletedExternally(task) },
                        onShareCompletedFile = { task -> shareCompletedFile(task) },
                        onOpenWebUrl = { url -> playStoreManager.openWebUrl(this, url) },
                        onTriggerPlayReview = { onDone ->
                            playStoreManager.launchInAppReview(this, onDone)
                        },
                        onOpenPlayStore = { playStoreManager.openPlayStoreListing(this) },
                        onCheckUpdatesManual = { onUpToDate, onFallback ->
                            playStoreManager.checkForUpdatesManual(
                                onUpToDate = onUpToDate,
                                onUnavailableFallback = {
                                    onFallback()
                                    playStoreManager.openPlayStoreListing(this)
                                }
                            )
                        },
                        onStartPlayUpdate = { info ->
                            playStoreManager.startUpdateFlow(
                                updateInfo = info,
                                launcher = inAppUpdateLauncher,
                                onFallbackOpenPlayStore = { playStoreManager.openPlayStoreListing(this) }
                            )
                        },
                        onDismissUpdatePrompt = { playStoreManager.dismissUpdatePrompt() },
                        onCompleteDownloadedUpdate = { playStoreManager.completeDownloadedUpdate() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::playStoreManager.isInitialized) {
            playStoreManager.checkForUpdatesSilently()
        }
    }

    override fun onDestroy() {
        if (::playStoreManager.isInitialized) {
            playStoreManager.unregisterListener()
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: intent.clipData?.getItemAt(0)?.text?.toString()
                    if (!sharedText.isNullOrBlank()) {
                        launchedFromExternalShare = true
                        viewModel.handleSharedText(sharedText, showNotification = true)
                    }
                }
            }
            ACTION_OPEN_SHARED_URL -> {
                val url = intent.getStringExtra(EXTRA_SHARED_URL)
                if (!url.isNullOrBlank()) {
                    launchedFromExternalShare = true
                    viewModel.handleSharedText(url, showNotification = false)
                }
            }
            ACTION_OPEN_DOWNLOADS -> {
                launchedFromExternalShare = true
                requestedTabFromIntent = 1
            }
            ACTION_OPEN_COMPLETED -> {
                launchedFromExternalShare = true
                requestedTabFromIntent = 2
            }
        }
    }

    private fun openCompletedExternally(task: DownloadTaskEntity): String? {
        val uri = task.localUri ?: return getString(R.string.file_missing_error)
        val storage = (application as SnapTubeApplication).storageManager
        val intent = storage.buildOpenExternalIntent(uri, task.formatEnum.mimeType)
        return runCatching {
            startActivity(intent)
            null
        }.getOrElse {
            getString(R.string.no_external_app)
        }
    }

    private fun shareCompletedFile(task: DownloadTaskEntity): String? {
        val uri = task.localUri ?: return getString(R.string.file_missing_error)
        val storage = (application as SnapTubeApplication).storageManager
        val intent = storage.buildShareFileIntent(uri, task.formatEnum.mimeType, task.title)
        return runCatching {
            startActivity(intent)
            null
        }.getOrElse {
            getString(R.string.file_missing_error)
        }
    }

    private fun createLocalizedContext(base: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    companion object {
        const val ACTION_OPEN_SHARED_URL = "com.example.snaptube.OPEN_SHARED_URL"
        const val ACTION_OPEN_DOWNLOADS = "com.example.snaptube.OPEN_DOWNLOADS"
        const val ACTION_OPEN_COMPLETED = "com.example.snaptube.OPEN_COMPLETED"
        const val EXTRA_SHARED_URL = "extra_shared_url"
        const val EXTRA_COMPLETED_TASK_ID = "extra_completed_task_id"
    }
}

private enum class SubScreen {
    NONE, ABOUT, LEGAL
}

@Composable
private fun SnapTubeRootApp(
    viewModel: SnapTubeViewModel,
    updateState: PlayUpdateState,
    skipSplash: Boolean,
    externalRequestedTab: Int?,
    onConsumeExternalTab: () -> Unit,
    onOpenExternalFile: (DownloadTaskEntity) -> String?,
    onShareCompletedFile: (DownloadTaskEntity) -> String?,
    onOpenWebUrl: (String) -> Unit,
    onTriggerPlayReview: (() -> Unit) -> Unit,
    onOpenPlayStore: () -> Unit,
    onCheckUpdatesManual: (onUpToDate: () -> Unit, onFallback: () -> Unit) -> Unit,
    onStartPlayUpdate: (com.google.android.play.core.appupdate.AppUpdateInfo) -> Unit,
    onDismissUpdatePrompt: () -> Unit,
    onCompleteDownloadedUpdate: () -> Unit
) {
    var showSplash by rememberSaveable { mutableStateOf(!skipSplash) }
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var subScreen by rememberSaveable { mutableStateOf(SubScreen.NONE) }
    var activeLegalDoc by rememberSaveable { mutableStateOf(LegalDocumentType.PRIVACY_POLICY) }
    var playingTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRateDialog by rememberSaveable { mutableStateOf(false) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val urlError by viewModel.urlError.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val showDownloadSheet by viewModel.showDownloadSheet.collectAsStateWithLifecycle()
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()

    val playingTask = remember(playingTaskId, completedTasks) {
        playingTaskId?.let { id -> completedTasks.firstOrNull { it.id == id } }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val updateReadyMsg = stringResource(R.string.update_downloaded_banner)
    val restartActionLabel = stringResource(R.string.update_action_restart)
    val upToDateMsg = stringResource(R.string.update_up_to_date)
    val playFallbackMsg = stringResource(R.string.update_play_store_fallback)
    val rateThanksMsg = stringResource(R.string.rate_thanks_message)
    val clipboardEmptyText = stringResource(R.string.clipboard_empty)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Respect user choice gracefully */ }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            viewModel.setDownloadFolder(treeUri)
        }
    }

    LaunchedEffect(skipSplash) {
        if (skipSplash) showSplash = false
    }

    LaunchedEffect(externalRequestedTab) {
        if (externalRequestedTab != null) {
            currentTab = externalRequestedTab
            subScreen = SubScreen.NONE
            playingTaskId = null
            onConsumeExternalTab()
        }
    }

    // Show interactive Restart Snackbar when flexible Play update finishes downloading
    LaunchedEffect(updateState) {
        if (updateState is PlayUpdateState.DownloadedReadyToInstall) {
            val result = snackbarHostState.showSnackbar(
                message = updateReadyMsg,
                actionLabel = restartActionLabel,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onCompleteDownloadedUpdate()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                UiEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                is UiEvent.SwitchTab -> {
                    currentTab = event.tabIndex
                    subScreen = SubScreen.NONE
                }
            }
        }
    }

    val handleCheckUpdates: () -> Unit = {
        onCheckUpdatesManual(
            {
                coroutineScope.launch { snackbarHostState.showSnackbar(upToDateMsg) }
            },
            {
                coroutineScope.launch { snackbarHostState.showSnackbar(playFallbackMsg) }
            }
        )
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    if (playingTask != null) {
        MediaPlayerScreen(
            task = playingTask,
            onBack = { playingTaskId = null },
            onShare = { task ->
                onShareCompletedFile(task)?.let { err ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                }
            },
            onOpenExternal = { task ->
                onOpenExternalFile(task)?.let { err ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                }
            }
        )
        return
    }

    if (subScreen == SubScreen.ABOUT) {
        AboutScreen(
            onBack = { subScreen = SubScreen.NONE },
            onOpenUrl = onOpenWebUrl,
            onRateApp = { showRateDialog = true },
            onCheckUpdates = handleCheckUpdates,
            onOpenLegal = { doc ->
                activeLegalDoc = doc
                subScreen = SubScreen.LEGAL
            }
        )
    } else if (subScreen == SubScreen.LEGAL) {
        LegalDocumentScreen(
            documentType = activeLegalDoc,
            onBack = { subScreen = SubScreen.NONE }
        )
    } else {
        if (currentTab != 0) {
            BackHandler { currentTab = 0 }
        }

        val navItems = listOf(
            Triple(stringResource(R.string.nav_home), Icons.Filled.Home, "nav_home"),
            Triple(stringResource(R.string.nav_downloads), SnapIcons.Download, "nav_downloads"),
            Triple(stringResource(R.string.nav_library), SnapIcons.Library, "nav_library"),
            Triple(stringResource(R.string.nav_settings), Icons.Filled.Settings, "nav_settings")
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItems.forEachIndexed { index, (label, icon, tag) ->
                                NavigationBarItem(
                                    selected = currentTab == index,
                                    onClick = { currentTab = index },
                                    modifier = Modifier.testTag(tag),
                                    icon = {
                                        NavIconWithBadge(
                                            icon = icon,
                                            label = label,
                                            badgeCount = if (index == 1) activeTasks.size else 0
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                SnapTubeLogoMark(
                                    size = 38.dp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        ) {
                            navItems.forEachIndexed { index, (label, icon, tag) ->
                                NavigationRailItem(
                                    selected = currentTab == index,
                                    onClick = { currentTab = index },
                                    modifier = Modifier.testTag(tag),
                                    icon = {
                                        NavIconWithBadge(
                                            icon = icon,
                                            label = label,
                                            badgeCount = if (index == 1) activeTasks.size else 0
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) }
                                )
                            }
                        }
                    }

                    when (currentTab) {
                        0 -> HomeScreen(
                            urlInput = urlInput,
                            urlError = urlError,
                            isAnalyzing = analysisState is AnalysisUiState.Loading,
                            recentDownloads = completedTasks,
                            onUrlChange = viewModel::onUrlInputChange,
                            onClearUrl = viewModel::clearUrlInput,
                            onAnalyze = { viewModel.analyzeUrl() },
                            onSampleSelected = { sampleUrl -> viewModel.analyzeUrl(sampleUrl) },
                            onClipboardEmpty = {
                                coroutineScope.launch { snackbarHostState.showSnackbar(clipboardEmptyText) }
                            },
                            onOpenSettings = { currentTab = 3 },
                            onSeeAllDownloads = { currentTab = 2 },
                            onOpenRecentFile = { task -> playingTaskId = task.id }
                        )
                        1 -> DownloadManagerScreen(
                            activeTasks = activeTasks,
                            completedTasks = completedTasks,
                            onPause = viewModel::pauseDownload,
                            onResume = viewModel::resumeDownload,
                            onRetry = viewModel::retryDownload,
                            onCancel = viewModel::cancelDownload,
                            onOpenCompleted = { task -> playingTaskId = task.id },
                            onShareCompleted = { task ->
                                onShareCompletedFile(task)?.let { err ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            }
                        )
                        2 -> CompletedLibraryScreen(
                            completedTasks = completedTasks,
                            onPlay = { task -> playingTaskId = task.id },
                            onOpenExternal = { task ->
                                onOpenExternalFile(task)?.let { err ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            },
                            onShare = { task ->
                                onShareCompletedFile(task)?.let { err ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(err) }
                                }
                            },
                            onRename = viewModel::renameCompletedFile,
                            onDelete = viewModel::deleteCompletedFile
                        )
                        3 -> SettingsScreen(
                            settings = settings,
                            completedTasks = completedTasks,
                            onThemeChange = viewModel::setThemeMode,
                            onLanguageChange = viewModel::setLanguage,
                            onDefaultVideoQualityChange = viewModel::setDefaultVideoQuality,
                            onDefaultAudioQualityChange = viewModel::setDefaultAudioQuality,
                            onPickDownloadFolder = { folderPickerLauncher.launch(null) },
                            onResetDownloadFolder = viewModel::resetDownloadFolder,
                            onMaxConcurrentChange = viewModel::setMaxConcurrentDownloads,
                            onWifiOnlyChange = viewModel::setWifiOnly,
                            onNotificationsChange = viewModel::setNotificationsEnabled,
                            onClearHistory = viewModel::clearDownloadHistory,
                            onRateApp = { showRateDialog = true },
                            onCheckUpdates = handleCheckUpdates,
                            onOpenAbout = { subScreen = SubScreen.ABOUT },
                            onOpenLegal = { doc ->
                                activeLegalDoc = doc
                                subScreen = SubScreen.LEGAL
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDownloadSheet) {
        DownloadOptionsBottomSheet(
            analysisState = analysisState,
            onSelectOption = viewModel::selectOption,
            onToggleMoreFormats = viewModel::toggleMoreFormats,
            onStartDownload = viewModel::startSelectedDownload,
            onRetry = { url -> viewModel.analyzeUrl(url) },
            onDismiss = viewModel::dismissDownloadSheet
        )
    }

    // Rate Us 5-Star Interactive Dialog
    if (showRateDialog) {
        RateSnapTubeDialog(
            onDismiss = { showRateDialog = false },
            onRatePlayStore = {
                showRateDialog = false
                onTriggerPlayReview {
                    coroutineScope.launch { snackbarHostState.showSnackbar(rateThanksMsg) }
                }
            },
            onOpenSupport = {
                showRateDialog = false
                onOpenWebUrl("mailto:support.webtecit@gmail.com")
            }
        )
    }

    // Automatic Play Store Update Available Prompt
    if (updateState is PlayUpdateState.UpdateAvailable) {
        AlertDialog(
            onDismissRequest = onDismissUpdatePrompt,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = SnapIcons.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.update_available_title),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.update_available_body),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                GradientPrimaryButton(
                    text = stringResource(R.string.update_action_now),
                    icon = SnapIcons.SystemUpdate,
                    onClick = {
                        onStartPlayUpdate(updateState.updateInfo)
                        onDismissUpdatePrompt()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "play_update_now_button"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissUpdatePrompt,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.update_action_later))
                }
            }
        )
    }
}

@Composable
private fun RateSnapTubeDialog(
    onDismiss: () -> Unit,
    onRatePlayStore: () -> Unit,
    onOpenSupport: () -> Unit
) {
    var selectedStars by rememberSaveable { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = { SnapTubeLogoMark(size = 52.dp, withContainer = true) },
        title = {
            Text(
                text = stringResource(R.string.rate_dialog_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.rate_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { selectedStars = star },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("rate_star_$star")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= selectedStars) {
                                    BrandGoldPrimary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (selectedStars >= 4) {
                    GradientPrimaryButton(
                        text = stringResource(R.string.rate_submit_play),
                        icon = Icons.Filled.Star,
                        onClick = onRatePlayStore,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "rate_submit_play_button"
                    )
                } else {
                    GradientPrimaryButton(
                        text = stringResource(R.string.rate_send_feedback),
                        icon = SnapIcons.Support,
                        onClick = onOpenSupport,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "rate_support_button"
                    )
                    TextButton(
                        onClick = onRatePlayStore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.rate_submit_play))
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.rate_later))
                }
            }
        }
    )
}

@Composable
private fun NavIconWithBadge(
    icon: ImageVector,
    label: String,
    badgeCount: Int
) {
    if (badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(if (badgeCount > 9) "9+" else "$badgeCount")
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(22.dp))
        }
    } else {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(22.dp))
    }
}
