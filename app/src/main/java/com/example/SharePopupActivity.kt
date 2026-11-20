package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.ui.SnapTubeViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.DownloadSheetBody
import com.example.ui.theme.LocalSnapTubeColors
import com.example.ui.theme.SnapTubeTheme
import java.util.Locale

/**
 * Translucent floating bottom-sheet Activity for ACTION_SEND links.
 * Opens directly on top of the source app (browser, video player, chat, etc.)
 * so users can choose a format and start downloading without leaving their current screen.
 */
class SharePopupActivity : ComponentActivity() {

    private val viewModel: SnapTubeViewModel by viewModels {
        SnapTubeViewModel.Factory(application as SnapTubeApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        processIncomingShare(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
            val localizedContext = remember(settings.language) {
                createLocalizedContext(this, settings.language)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalActivityResultRegistryOwner provides this@SharePopupActivity,
                LocalOnBackPressedDispatcherOwner provides this@SharePopupActivity
            ) {
                SnapTubeTheme(themeMode = settings.themeMode) {
                    val notificationLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { /* Notification preference respected */ }

                    LaunchedEffect(Unit) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is UiEvent.ShowMessage -> {
                                    Toast.makeText(this@SharePopupActivity, event.message, Toast.LENGTH_SHORT).show()
                                }
                                UiEvent.RequestNotificationPermission -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                is UiEvent.SwitchTab -> {
                                    // Download was queued cleanly — dismiss overlay back to source app
                                    finishAndRemoveTask()
                                }
                            }
                        }
                    }

                    FloatingDownloadPopupOverlay(
                        onDismiss = { finish() },
                        content = {
                            DownloadSheetBody(
                                analysisState = analysisState,
                                onSelectOption = viewModel::selectOption,
                                onToggleMoreFormats = viewModel::toggleMoreFormats,
                                onStartDownload = viewModel::startSelectedDownload,
                                onRetry = { url -> viewModel.analyzeUrl(url, openSheetImmediately = true) },
                                onDismiss = { finish() },
                                onOpenApp = {
                                    val launchIntent = Intent(this@SharePopupActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }
                                    startActivity(launchIntent)
                                    finish()
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processIncomingShare(intent)
    }

    private fun processIncomingShare(intent: Intent?) {
        if (intent == null) {
            finish()
            return
        }
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
        if (sharedText.isNullOrBlank()) {
            Toast.makeText(this, R.string.invalid_url_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.handleSharedText(sharedText, showNotification = false)
    }

    private fun createLocalizedContext(base: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

@Composable
private fun FloatingDownloadPopupOverlay(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    val sheetBg = if (extended.isDark) extended.cardElevated else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = sheetBg,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sleek top drag handle indicator
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 6.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                )
                content()
            }
        }
    }
}
