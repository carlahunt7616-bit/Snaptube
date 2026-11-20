package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FormatUtils
import com.example.data.model.LegalDocumentType
import com.example.data.model.QualityPresets
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import com.example.ui.components.SnapIcons
import com.example.ui.theme.LocalSnapTubeColors

@Composable
fun SettingsScreen(
    settings: UserSettings,
    completedTasks: List<DownloadTaskEntity>,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onDefaultVideoQualityChange: (String) -> Unit,
    onDefaultAudioQualityChange: (String) -> Unit,
    onPickDownloadFolder: () -> Unit,
    onResetDownloadFolder: () -> Unit,
    onMaxConcurrentChange: (Int) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onRateApp: () -> Unit,
    onCheckUpdates: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLegal: (LegalDocumentType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val totalDownloadedBytes = remember(completedTasks) {
        completedTasks.sumOf { it.totalBytes.coerceAtLeast(0L) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Appearance
            item {
                SettingsGroupCard(title = stringResource(R.string.settings_section_appearance)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = SnapIcons.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_section_appearance),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val label = when (mode) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                }
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { onThemeChange(mode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("theme_chip_${mode.key}"),
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Language
            item {
                SettingsGroupCard(title = stringResource(R.string.settings_section_language)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = SnapIcons.Globe,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_section_language),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = settings.language == AppLanguage.ENGLISH,
                                onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_chip_en"),
                                label = { Text(stringResource(R.string.lang_english)) }
                            )
                            FilterChip(
                                selected = settings.language == AppLanguage.BENGALI,
                                onClick = { onLanguageChange(AppLanguage.BENGALI) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_chip_bn"),
                                label = { Text(stringResource(R.string.lang_bengali)) }
                            )
                        }
                    }
                }
            }

            // Download Settings
            item {
                SettingsGroupCard(title = stringResource(R.string.settings_section_downloads)) {
                    SettingsNavRow(
                        icon = SnapIcons.VideoFile,
                        title = stringResource(R.string.setting_default_video_quality),
                        subtitle = settings.defaultVideoQuality,
                        onClick = { showVideoQualityDialog = true },
                        testTag = "setting_video_quality"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = SnapIcons.MusicNote,
                        title = stringResource(R.string.setting_default_audio_quality),
                        subtitle = settings.defaultAudioQuality,
                        onClick = { showAudioQualityDialog = true },
                        testTag = "setting_audio_quality"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onPickDownloadFolder)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("setting_download_folder"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = SnapIcons.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_download_folder),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = settings.downloadFolderLabel
                                    ?: stringResource(R.string.default_folder_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (settings.downloadFolderUri != null) {
                            TextButton(onClick = onResetDownloadFolder) {
                                Text(stringResource(R.string.reset_default_folder))
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = SnapIcons.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = stringResource(R.string.setting_simultaneous_limit),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..4).forEach { count ->
                                FilterChip(
                                    selected = settings.maxConcurrentDownloads == count,
                                    onClick = { onMaxConcurrentChange(count) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("concurrent_limit_$count"),
                                    label = { Text("$count") }
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsSwitchRow(
                        icon = SnapIcons.Wifi,
                        title = stringResource(R.string.setting_wifi_only),
                        subtitle = stringResource(R.string.setting_wifi_only_sub),
                        checked = settings.wifiOnly,
                        onCheckedChange = onWifiOnlyChange,
                        testTag = "switch_wifi_only"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsSwitchRow(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.setting_notifications),
                        subtitle = stringResource(R.string.setting_notifications_sub),
                        checked = settings.notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                        testTag = "switch_notifications"
                    )
                }
            }

            // Storage
            item {
                SettingsGroupCard(title = stringResource(R.string.settings_section_storage)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = SnapIcons.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.storage_used_label),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(
                                    R.string.storage_files_count,
                                    completedTasks.size,
                                    FormatUtils.formatBytes(totalDownloadedBytes)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = Icons.Filled.Delete,
                        title = stringResource(R.string.setting_clear_history),
                        subtitle = stringResource(R.string.setting_clear_history_sub),
                        onClick = { showClearHistoryDialog = true },
                        testTag = "setting_clear_history"
                    )
                }
            }

            // Other & About
            item {
                SettingsGroupCard(title = stringResource(R.string.settings_section_other)) {
                    SettingsNavRow(
                        icon = Icons.Filled.Star,
                        title = stringResource(R.string.setting_rate_us),
                        subtitle = stringResource(R.string.setting_rate_us_sub),
                        onClick = onRateApp,
                        testTag = "setting_rate_us"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = SnapIcons.SystemUpdate,
                        title = stringResource(R.string.setting_check_update),
                        subtitle = stringResource(R.string.setting_check_update_sub),
                        onClick = onCheckUpdates,
                        testTag = "setting_check_update"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.setting_about),
                        subtitle = "${stringResource(R.string.setting_version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        onClick = onOpenAbout,
                        testTag = "setting_about"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = SnapIcons.ShieldCheck,
                        title = stringResource(R.string.setting_privacy),
                        subtitle = null,
                        onClick = { onOpenLegal(LegalDocumentType.PRIVACY_POLICY) },
                        testTag = "setting_privacy"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = SnapIcons.ShieldCheck,
                        title = stringResource(R.string.setting_terms),
                        subtitle = null,
                        onClick = { onOpenLegal(LegalDocumentType.TERMS_AND_CONDITIONS) },
                        testTag = "setting_terms"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.setting_disclaimer),
                        subtitle = null,
                        onClick = { onOpenLegal(LegalDocumentType.DISCLAIMER) },
                        testTag = "setting_disclaimer"
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showVideoQualityDialog) {
        QualityPickerDialog(
            title = stringResource(R.string.setting_default_video_quality),
            options = QualityPresets.videoQualities,
            selected = settings.defaultVideoQuality,
            onSelect = {
                onDefaultVideoQualityChange(it)
                showVideoQualityDialog = false
            },
            onDismiss = { showVideoQualityDialog = false }
        )
    }

    if (showAudioQualityDialog) {
        QualityPickerDialog(
            title = stringResource(R.string.setting_default_audio_quality),
            options = QualityPresets.audioQualities,
            selected = settings.defaultAudioQuality,
            onSelect = {
                onDefaultAudioQualityChange(it)
                showAudioQualityDialog = false
            },
            onDismiss = { showAudioQualityDialog = false }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_clear),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = extended.cardElevated,
            border = BorderStroke(1.dp, extended.subtleBorder)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun QualityPickerDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = item == selected,
                            onClick = { onSelect(item) }
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
