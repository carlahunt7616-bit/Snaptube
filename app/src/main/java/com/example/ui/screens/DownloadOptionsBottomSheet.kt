package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.BrandGoldPrimary
import com.example.ui.theme.LocalSnapTubeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsBottomSheet(
    analysisState: AnalysisUiState,
    onSelectOption: (String) -> Unit,
    onToggleMoreFormats: () -> Unit,
    onStartDownload: () -> Unit,
    onRetry: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenApp: (() -> Unit)? = null
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
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                width = 40.dp,
                height = 4.dp
            )
        }
    ) {
        DownloadSheetBody(
            analysisState = analysisState,
            onSelectOption = onSelectOption,
            onToggleMoreFormats = onToggleMoreFormats,
            onStartDownload = onStartDownload,
            onRetry = onRetry,
            onDismiss = onDismiss,
            onOpenApp = onOpenApp
        )
    }
}

@Composable
fun DownloadSheetBody(
    analysisState: AnalysisUiState,
    onSelectOption: (String) -> Unit,
    onToggleMoreFormats: () -> Unit,
    onStartDownload: () -> Unit,
    onRetry: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenApp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp)
            .verticalScroll(rememberScrollState())
            .testTag("download_options_sheet"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.download_sheet_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
                        onClose = onDismiss,
                        onOpenApp = onOpenApp
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
            color = BrandGoldPrimary,
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
                    shape = RoundedCornerShape(50)
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
        shape = RoundedCornerShape(16.dp),
        color = if (extended.isDark) MaterialTheme.colorScheme.surface else Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, extended.subtleBorder.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MediaThumbnailBox(
                thumbnailUrl = media.thumbnailUrl,
                mediaType = media.options.firstOrNull()?.mediaType ?: MediaType.VIDEO,
                modifier = Modifier.size(width = 84.dp, height = 60.dp),
                cornerRadius = 10.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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
    onClose: () -> Unit,
    onOpenApp: (() -> Unit)? = null
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(stringResource(R.string.action_close))
                }
                if (onOpenApp != null) {
                    GradientPrimaryButton(
                        text = stringResource(R.string.action_open_app),
                        onClick = onOpenApp,
                        modifier = Modifier.weight(1f),
                        testTag = "sheet_open_app_button"
                    )
                }
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (audioOptions.isNotEmpty()) {
            SectionLabel(title = stringResource(R.string.section_music))
            audioOptions.forEach { opt ->
                CleanQualityRow(
                    option = opt,
                    selected = opt.id == selectedOptionId,
                    onClick = { onSelectOption(opt.id) }
                )
            }
        }

        if (audioOptions.isNotEmpty() && videoOptions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
        }

        if (videoOptions.isNotEmpty()) {
            SectionLabel(title = stringResource(R.string.section_video))
            videoOptions.forEach { opt ->
                CleanQualityRow(
                    option = opt,
                    selected = opt.id == selectedOptionId,
                    onClick = { onSelectOption(opt.id) }
                )
            }
        }

        AnimatedVisibility(visible = extraOptions.isNotEmpty()) {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onToggleMoreFormats)
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("more_formats_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            if (showMoreFormats) R.string.less_formats else R.string.more_formats
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.formats_all),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (showMoreFormats) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun CleanQualityRow(
    option: MediaOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isAudio = option.mediaType == MediaType.AUDIO

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quality_option_${option.id}")
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) BrandGoldPrimary.copy(alpha = 0.08f) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isAudio) SnapIcons.MusicNote else SnapIcons.VideoFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(16.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = option.qualityLabel,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = option.format.ext.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = if (option.estimatedBytes > 0L) {
                FormatUtils.formatBytes(option.estimatedBytes)
            } else {
                stringResource(R.string.size_unknown)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(14.dp))

        // Signature Golden Yellow circular checkmark selector
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BrandGoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF1A1205),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.6.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            )
        }
    }
}
