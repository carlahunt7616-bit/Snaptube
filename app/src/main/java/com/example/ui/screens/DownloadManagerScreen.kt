package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.FormatUtils
import com.example.ui.components.MediaThumbnailBox
import com.example.ui.components.SnapIcons
import com.example.ui.theme.LocalSnapTubeColors

@Composable
fun DownloadManagerScreen(
    activeTasks: List<DownloadTaskEntity>,
    completedTasks: List<DownloadTaskEntity>,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onOpenCompleted: (DownloadTaskEntity) -> Unit,
    onShareCompleted: (DownloadTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val downloadingCount = activeTasks.count { it.statusEnum == DownloadStatus.DOWNLOADING }
    val queuedCount = activeTasks.count { it.statusEnum == DownloadStatus.QUEUED }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("download_manager_screen")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.nav_downloads),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (activeTasks.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.queue_summary_format, downloadingCount, queuedCount),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Downloading / Completed Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_downloading"),
                    text = {
                        Text(
                            text = "${stringResource(R.string.tab_downloading)} (${activeTasks.size})",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_completed_in_manager"),
                    text = {
                        Text(
                            text = "${stringResource(R.string.tab_completed)} (${completedTasks.size})",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                if (activeTasks.isEmpty()) {
                    EmptyDownloadState(
                        title = stringResource(R.string.empty_downloading_title),
                        subtitle = stringResource(R.string.empty_downloading_subtitle)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeTasks, key = { it.id }) { task ->
                            ActiveDownloadCard(
                                task = task,
                                onPause = { onPause(task.id) },
                                onResume = { onResume(task.id) },
                                onRetry = { onRetry(task.id) },
                                onCancel = { onCancel(task.id) }
                            )
                        }
                    }
                }
            } else {
                if (completedTasks.isEmpty()) {
                    EmptyDownloadState(
                        title = stringResource(R.string.empty_library_title),
                        subtitle = stringResource(R.string.empty_library_subtitle)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(completedTasks, key = { it.id }) { task ->
                            QuickCompletedCard(
                                task = task,
                                onOpen = { onOpenCompleted(task) },
                                onShare = { onShareCompleted(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveDownloadCard(
    task: DownloadTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    val status = task.statusEnum
    val animatedProgress by animateFloatAsState(
        targetValue = task.progressFraction,
        label = "dl_progress_${task.id}"
    )

    val statusColor = when (status) {
        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
        DownloadStatus.QUEUED -> extended.blueAccent
        DownloadStatus.PAUSED -> extended.warning
        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
        else -> extended.success
    }

    val statusText = when (status) {
        DownloadStatus.QUEUED -> stringResource(R.string.status_queued)
        DownloadStatus.DOWNLOADING -> stringResource(R.string.status_downloading)
        DownloadStatus.PAUSED -> stringResource(R.string.status_paused)
        DownloadStatus.FAILED -> stringResource(R.string.status_failed)
        DownloadStatus.COMPLETED -> stringResource(R.string.status_completed)
        DownloadStatus.CANCELED -> stringResource(R.string.status_canceled)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_download_card_${task.id}"),
        shape = RoundedCornerShape(18.dp),
        color = extended.cardElevated,
        border = BorderStroke(1.dp, extended.subtleBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MediaThumbnailBox(
                    thumbnailUrl = task.thumbnailUrl,
                    mediaType = task.mediaTypeEnum,
                    modifier = Modifier.size(56.dp),
                    cornerRadius = 12.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${task.qualityLabel} • ${task.formatExt.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action controls: Pause / Resume / Retry + Cancel
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    when (status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("pause_download_${task.id}")
                            ) {
                                Icon(
                                    imageVector = SnapIcons.Pause,
                                    contentDescription = stringResource(R.string.action_pause),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("resume_download_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = stringResource(R.string.action_resume),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = onRetry,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("retry_download_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = stringResource(R.string.action_retry),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        else -> Unit
                    }

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cancel_download_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Gradient progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(extended.brandGradient)
                )
            }

            // Metrics row: downloaded / total, percentage, speed, ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val downloadedStr = FormatUtils.formatBytes(task.downloadedBytes)
                val totalStr = if (task.totalBytes > 0L) FormatUtils.formatBytes(task.totalBytes) else "—"
                Text(
                    text = "$downloadedStr / $totalStr (${task.progressPercent}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val speedAndEta = buildString {
                    if (status == DownloadStatus.DOWNLOADING && task.speedBytesPerSec > 0L) {
                        append(FormatUtils.formatSpeed(task.speedBytesPerSec))
                        val etaStr = FormatUtils.formatEta(task.etaSeconds)
                        if (etaStr.isNotBlank()) {
                            append(" • ")
                            append(etaStr)
                        }
                    } else if (!task.errorMessage.isNullOrBlank()) {
                        append(task.errorMessage)
                    }
                }
                if (speedAndEta.isNotBlank()) {
                    Text(
                        text = speedAndEta,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status == DownloadStatus.FAILED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCompletedCard(
    task: DownloadTaskEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = extended.cardElevated,
        border = BorderStroke(1.dp, extended.subtleBorder),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MediaThumbnailBox(
                thumbnailUrl = task.thumbnailUrl,
                mediaType = task.mediaTypeEnum,
                modifier = Modifier.size(54.dp),
                cornerRadius = 12.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${task.qualityLabel} • ${task.formatExt.uppercase()} • ${FormatUtils.formatBytes(task.totalBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onOpen,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.player_play),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyDownloadState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val extended = LocalSnapTubeColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SnapIcons.Download,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
