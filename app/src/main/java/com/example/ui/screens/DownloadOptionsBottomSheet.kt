package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AnalyzedMedia
import com.example.data.model.FormatUtils
import com.example.data.model.MediaOption
import com.example.data.model.MediaType
import com.example.ui.AnalysisUiState
import com.example.ui.components.GradientPrimaryButton
import com.example.ui.components.MediaThumbnailBox
import com.example.ui.components.PlatformBadge
import com.example.ui.components.SnapIcons
import com.example.ui.theme.LocalSnapTubeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsBottomSheet(
    analysisState: AnalysisUiState,
    onSelectOption: (String) -> Unit,
    onToggleMoreFormats: () -> Unit,
    onStartDownload: () -> Unit,
    onRetry: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extended = LocalSnapTubeColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (extended.isDark) extended.cardElevated else Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline,
                width = 40.dp,
                height = 4.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("download_options_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.download_sheet_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (analysisState) {
                is AnalysisUiState.Idle -> Unit

                is AnalysisUiState.Loading -> {
                    LoadingAnalysisView(url = analysisState.url)
                }

                is AnalysisUiState.Error -> {
                    ErrorAnalysisView(
                        message = analysisState.message,
                        onRetry = { onRetry(analysisState.url) },
                        onClose = onDismiss
                    )
                }

                is AnalysisUiState.Ready -> {
                    MediaPreviewHeader(media = analysisState.media)

                    if (!analysisState.media.isSupportedForDownload || analysisState.media.options.isEmpty()) {
                        UnsupportedSourceCard(
                            reason = analysisState.media.unsupportedReason
                                ?: stringResource(R.string.unsupported_source_hint),
                            onClose = onDismiss
                        )
                    } else {
                        DownloadFormatsSelector(
                            options = analysisState.media.options,
                            selectedOptionId = analysisState.selectedOptionId,
                            showMoreFormats = analysisState.showMoreFormats,
                            onSelectOption = onSelectOption,
                            onToggleMoreFormats = onToggleMoreFormats,
                            onStartDownload = onStartDownload
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAnalysisView(url: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(38.dp)
        )
        Text(
            text = stringResource(R.string.action_analyzing),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = url,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ErrorAnalysisView(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.analysis_failed_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.action_close))
                }
                GradientPrimaryButton(
                    text = stringResource(R.string.action_retry),
                    icon = Icons.Filled.Refresh,
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    testTag = "retry_analysis_button"
                )
            }
        }
    }
}

@Composable
private fun MediaPreviewHeader(media: AnalyzedMedia) {
    val extended = LocalSnapTubeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.subtleBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MediaThumbnailBox(
                thumbnailUrl = media.thumbnailUrl,
                mediaType = media.options.firstOrNull()?.mediaType ?: MediaType.VIDEO,
                modifier = Modifier.size(width = 88.dp, height = 66.dp),
                cornerRadius = 12.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformBadge(platform = media.platform, compact = true)
                    if (media.primarySizeBytes > 0L) {
                        Text(
                            text = FormatUtils.formatBytes(media.primarySizeBytes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!media.durationLabel.isNullOrBlank()) {
                        Text(
                            text = "• ${media.durationLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnsupportedSourceCard(
    reason: String,
    onClose: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unsupported_source_card"),
        shape = RoundedCornerShape(18.dp),
        color = extended.warningContainer.copy(alpha = if (extended.isDark) 0.45f else 0.7f),
        border = BorderStroke(1.dp, extended.warning.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = SnapIcons.ShieldCheck,
                    contentDescription = null,
                    tint = extended.warning,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.unsupported_source_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.unsupported_source_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.action_close))
            }
        }
    }
}

@Composable
private fun DownloadFormatsSelector(
    options: List<MediaOption>,
    selectedOptionId: String?,
    showMoreFormats: Boolean,
    onSelectOption: (String) -> Unit,
    onToggleMoreFormats: () -> Unit,
    onStartDownload: () -> Unit
) {
    val primaryOptions = options.filter { !it.isExtraFormat }
    val extraOptions = options.filter { it.isExtraFormat }
    val visibleOptions = if (showMoreFormats || primaryOptions.isEmpty()) options else primaryOptions

    val audioOptions = visibleOptions.filter { it.mediaType == MediaType.AUDIO }
    val videoOptions = visibleOptions.filter { it.mediaType == MediaType.VIDEO }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (audioOptions.isNotEmpty()) {
            QualitySectionHeader(
                icon = SnapIcons.MusicNote,
                title = stringResource(R.string.section_music)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                audioOptions.forEach { opt ->
                    QualityOptionRow(
                        option = opt,
                        selected = opt.id == selectedOptionId,
                        onClick = { onSelectOption(opt.id) }
                    )
                }
            }
        }

        if (audioOptions.isNotEmpty() && videoOptions.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (videoOptions.isNotEmpty()) {
            QualitySectionHeader(
                icon = SnapIcons.VideoFile,
                title = stringResource(R.string.section_video)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                videoOptions.forEach { opt ->
                    QualityOptionRow(
                        option = opt,
                        selected = opt.id == selectedOptionId,
                        onClick = { onSelectOption(opt.id) }
                    )
                }
            }
        }

        AnimatedVisibility(visible = extraOptions.isNotEmpty()) {
            TextButton(
                onClick = onToggleMoreFormats,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("more_formats_button")
            ) {
                Text(
                    text = stringResource(
                        if (showMoreFormats) R.string.less_formats else R.string.more_formats
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (showMoreFormats) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        GradientPrimaryButton(
            text = stringResource(R.string.start_download),
            icon = SnapIcons.Download,
            enabled = selectedOptionId != null,
            onClick = onStartDownload,
            modifier = Modifier.fillMaxWidth(),
            testTag = "sheet_start_download_button"
        )
    }
}

@Composable
private fun QualitySectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QualityOptionRow(
    option: MediaOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else extended.subtleBorder
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (extended.isDark) 0.45f else 0.55f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quality_option_${option.id}")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = option.qualityLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = option.format.ext.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = if (option.estimatedBytes > 0L) {
                    FormatUtils.formatBytes(option.estimatedBytes)
                } else {
                    stringResource(R.string.size_unknown)
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
