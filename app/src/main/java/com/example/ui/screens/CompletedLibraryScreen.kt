package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.FormatUtils
import com.example.data.model.MediaType
import com.example.data.model.SortOption
import com.example.ui.components.MediaThumbnailBox
import com.example.ui.components.SnapIcons
import com.example.ui.theme.LocalSnapTubeColors

@Composable
fun CompletedLibraryScreen(
    completedTasks: List<DownloadTaskEntity>,
    onPlay: (DownloadTaskEntity) -> Unit,
    onOpenExternal: (DownloadTaskEntity) -> Unit,
    onShare: (DownloadTaskEntity) -> Unit,
    onRename: (DownloadTaskEntity, String) -> Unit,
    onDelete: (DownloadTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Videos, 1 = Audio
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortOption by rememberSaveable { mutableStateOf(SortOption.NEWEST) }

    var renameTarget by remember { mutableStateOf<DownloadTaskEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<DownloadTaskEntity?>(null) }

    val videoCount = remember(completedTasks) { completedTasks.count { it.mediaTypeEnum == MediaType.VIDEO } }
    val audioCount = remember(completedTasks) { completedTasks.count { it.mediaTypeEnum == MediaType.AUDIO } }

    val filteredTasks by remember(completedTasks, selectedTab, searchQuery, sortOption) {
        derivedStateOf {
            val targetType = if (selectedTab == 0) MediaType.VIDEO else MediaType.AUDIO
            val base = completedTasks.filter { task ->
                task.mediaTypeEnum == targetType &&
                    (searchQuery.isBlank() ||
                        task.title.contains(searchQuery, ignoreCase = true) ||
                        task.fileName.contains(searchQuery, ignoreCase = true))
            }
            when (sortOption) {
                SortOption.NEWEST -> base.sortedByDescending { it.completedAt ?: it.createdAt }
                SortOption.NAME -> base.sortedBy { it.title.lowercase() }
                SortOption.SIZE -> base.sortedByDescending { it.totalBytes }
            }
        }
    }

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
                .testTag("completed_library_screen")
        ) {
            Text(
                text = stringResource(R.string.nav_library),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Videos / Audio Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("library_tab_videos"),
                    text = {
                        Text(
                            text = "${stringResource(R.string.tab_videos)} ($videoCount)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("library_tab_audio"),
                    text = {
                        Text(
                            text = "${stringResource(R.string.tab_audio)} ($audioCount)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }

            // Search & Sort Toolbar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_field"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_downloads_hint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.action_clear)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = SnapIcons.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    FilterChip(
                        selected = sortOption == SortOption.NEWEST,
                        onClick = { sortOption = SortOption.NEWEST },
                        label = { Text(stringResource(R.string.sort_newest)) }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.NAME,
                        onClick = { sortOption = SortOption.NAME },
                        label = { Text(stringResource(R.string.sort_name)) }
                    )
                    FilterChip(
                        selected = sortOption == SortOption.SIZE,
                        onClick = { sortOption = SortOption.SIZE },
                        label = { Text(stringResource(R.string.sort_size)) }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                EmptyDownloadState(
                    title = stringResource(R.string.empty_library_title),
                    subtitle = stringResource(R.string.empty_library_subtitle)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        CompletedFileCard(
                            task = task,
                            onPlay = { onPlay(task) },
                            onOpenExternal = { onOpenExternal(task) },
                            onShare = { onShare(task) },
                            onRequestRename = {
                                renameTarget = task
                                renameText = task.title
                            },
                            onRequestDelete = { deleteTarget = task }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }

    // Rename Dialog
    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.rename_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(stringResource(R.string.rename_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(target, renameText)
                        renameTarget = null
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = {
                Text(stringResource(R.string.delete_dialog_body, target.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target)
                        deleteTarget = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun CompletedFileCard(
    task: DownloadTaskEntity,
    onPlay: () -> Unit,
    onOpenExternal: () -> Unit,
    onShare: () -> Unit,
    onRequestRename: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("completed_file_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        color = extended.cardElevated,
        border = BorderStroke(1.dp, extended.subtleBorder),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MediaThumbnailBox(
                thumbnailUrl = task.thumbnailUrl,
                mediaType = task.mediaTypeEnum,
                modifier = Modifier.size(58.dp),
                cornerRadius = 12.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.qualityLabel} • ${task.formatExt.uppercase()} • ${FormatUtils.formatBytes(task.totalBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = FormatUtils.formatDate(task.completedAt ?: task.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("completed_more_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.cd_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_play)) },
                        leadingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_open_external)) },
                        leadingIcon = { Icon(SnapIcons.OpenInNew, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpenExternal()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share)) },
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRequestRename()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.action_delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRequestDelete()
                        }
                    )
                }
            }
        }
    }
}
