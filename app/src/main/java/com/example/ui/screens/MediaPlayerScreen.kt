package com.example.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.FormatUtils
import com.example.data.model.MediaType
import com.example.ui.components.GradientPrimaryButton
import com.example.ui.components.SnapIcons
import com.example.ui.theme.BrandCoralRed
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandGoldPrimary
import com.example.ui.theme.LocalSnapTubeColors
import com.example.ui.theme.SnapBlue
import com.example.ui.theme.SnapPurple
import kotlinx.coroutines.delay

@Composable
fun MediaPlayerScreen(
    task: DownloadTaskEntity,
    onBack: () -> Unit,
    onShare: (DownloadTaskEntity) -> Unit,
    onOpenExternal: (DownloadTaskEntity) -> Unit
) {
    BackHandler(onBack = onBack)

    if (task.mediaTypeEnum == MediaType.VIDEO) {
        VideoPlayerView(
            task = task,
            onBack = onBack,
            onShare = { onShare(task) },
            onOpenExternal = { onOpenExternal(task) }
        )
    } else {
        AudioPlayerView(
            task = task,
            onBack = onBack,
            onShare = { onShare(task) },
            onOpenExternal = { onOpenExternal(task) }
        )
    }
}

@Composable
private fun VideoPlayerView(
    task: DownloadTaskEntity,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(task.localUri.isNullOrBlank()) }
    var durationMs by remember { mutableIntStateOf(0) }
    var positionMs by remember { mutableIntStateOf(0) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Poll live playback position every 250ms while playing
    LaunchedEffect(isPlaying, isPrepared) {
        while (isPrepared && isPlaying) {
            val view = videoViewRef
            if (view != null && !isDraggingSlider) {
                runCatching { positionMs = view.currentPosition.coerceAtLeast(0) }
            }
            delay(250L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { videoViewRef?.stopPlayback() }
            videoViewRef = null
            mediaPlayerRef = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C10))
            .testTag("video_player_screen")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controlsVisible = !controlsVisible }
    ) {
        if (!hasError && !task.localUri.isNullOrBlank()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .aspectRatio(16f / 9f),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            mp.isLooping = isLooping
                            durationMs = mp.duration.coerceAtLeast(0)
                            isPrepared = true
                            start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            if (!isLooping) {
                                isPlaying = false
                                positionMs = durationMs
                                controlsVisible = true
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            hasError = true
                            isPlaying = false
                            true
                        }
                        runCatching {
                            setVideoURI(Uri.parse(task.localUri))
                        }.onFailure {
                            hasError = true
                        }
                    }
                }
            )
        }

        if (!isPrepared && !hasError) {
            CircularProgressIndicator(
                color = BrandGoldPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (hasError) {
            PlayerErrorOverlay(
                onOpenExternal = onOpenExternal,
                onBack = onBack,
                darkSurface = true
            )
        }

        // Top Bar Overlay
        AnimatedVisibility(
            visible = controlsVisible || hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.qualityLabel} • ${task.formatExt.uppercase()} • ${FormatUtils.formatBytes(task.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.action_share),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onOpenExternal) {
                    Icon(
                        imageVector = SnapIcons.OpenInNew,
                        contentDescription = stringResource(R.string.action_open_external),
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible && !hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.86f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Seek Bar
                val safeDuration = durationMs.coerceAtLeast(1)
                Slider(
                    value = positionMs.coerceIn(0, safeDuration).toFloat(),
                    onValueChange = { newVal ->
                        isDraggingSlider = true
                        positionMs = newVal.toInt()
                    },
                    onValueChangeFinished = {
                        videoViewRef?.seekTo(positionMs)
                        isDraggingSlider = false
                    },
                    valueRange = 0f..safeDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = BrandGoldPrimary,
                        activeTrackColor = BrandGoldPrimary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FormatUtils.formatDuration((positionMs / 1000L).coerceAtLeast(0L)).ifBlank { "00:00" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = FormatUtils.formatDuration((durationMs / 1000L).coerceAtLeast(0L)).ifBlank { "00:00" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Transport Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            val vol = if (isMuted) 0f else 1f
                            runCatching { mediaPlayerRef?.setVolume(vol, vol) }
                        }
                    ) {
                        Icon(
                            imageVector = if (isMuted) SnapIcons.VolumeOff else SnapIcons.VolumeUp,
                            contentDescription = stringResource(if (isMuted) R.string.player_unmute else R.string.player_mute),
                            tint = if (isMuted) BrandCoralRed else Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            val target = (positionMs - 10_000).coerceAtLeast(0)
                            positionMs = target
                            videoViewRef?.seekTo(target)
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Replay10,
                            contentDescription = stringResource(R.string.player_rewind_10),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            val view = videoViewRef ?: return@Surface
                            if (isPlaying) {
                                view.pause()
                                isPlaying = false
                            } else {
                                if (positionMs >= durationMs && durationMs > 0) {
                                    view.seekTo(0)
                                    positionMs = 0
                                }
                                view.start()
                                isPlaying = true
                            }
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .testTag("player_play_pause_button"),
                        shape = CircleShape,
                        color = BrandGoldPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) SnapIcons.Pause else Icons.Filled.PlayArrow,
                                contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                                tint = Color(0xFF1A1205),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val target = (positionMs + 10_000).coerceAtMost(durationMs)
                            positionMs = target
                            videoViewRef?.seekTo(target)
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Forward10,
                            contentDescription = stringResource(R.string.player_forward_10),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isLooping = !isLooping
                            runCatching { mediaPlayerRef?.isLooping = isLooping }
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Repeat,
                            contentDescription = stringResource(if (isLooping) R.string.player_loop_on else R.string.player_loop_off),
                            tint = if (isLooping) BrandGoldPrimary else Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerView(
    task: DownloadTaskEntity,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val context = LocalContext.current
    val extended = LocalSnapTubeColors.current

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(task.localUri.isNullOrBlank()) }
    var durationMs by remember { mutableIntStateOf(0) }
    var positionMs by remember { mutableIntStateOf(0) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "audio_pulse")
    val discScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disc_scale"
    )

    DisposableEffect(task.localUri) {
        val uriString = task.localUri
        if (uriString.isNullOrBlank()) {
            hasError = true
            return@DisposableEffect onDispose { }
        }
        val mp = MediaPlayer()
        mediaPlayer = mp
        runCatching {
            mp.setDataSource(context, Uri.parse(uriString))
            mp.setOnPreparedListener { player ->
                durationMs = player.duration.coerceAtLeast(0)
                isPrepared = true
                player.isLooping = isLooping
                player.start()
                isPlaying = true
            }
            mp.setOnCompletionListener {
                if (!isLooping) {
                    isPlaying = false
                    positionMs = durationMs
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                hasError = true
                isPlaying = false
                true
            }
            mp.prepareAsync()
        }.onFailure {
            hasError = true
        }

        onDispose {
            runCatching {
                if (mp.isPlaying) mp.stop()
                mp.release()
            }
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying, isPrepared) {
        while (isPrepared && isPlaying) {
            val mp = mediaPlayer
            if (mp != null && !isDraggingSlider) {
                runCatching { positionMs = mp.currentPosition.coerceAtLeast(0) }
            }
            delay(250L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("audio_player_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.player_now_playing),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.action_share),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onOpenExternal) {
                        Icon(
                            imageVector = SnapIcons.OpenInNew,
                            contentDescription = stringResource(R.string.action_open_external),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (hasError) {
                Spacer(Modifier.weight(1f))
                PlayerErrorOverlay(
                    onOpenExternal = onOpenExternal,
                    onBack = onBack,
                    darkSurface = false
                )
                Spacer(Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(0.7f))

                // Stylized Glowing Audio Artwork Disc
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(if (isPlaying) discScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(BrandGoldLight.copy(alpha = 0.35f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(44.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BrandGoldLight, BrandGoldPrimary, BrandCoralRed)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(92.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.94f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = SnapIcons.MusicNote,
                                    contentDescription = null,
                                    tint = BrandCoralRed,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Title & Quality Meta
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, extended.subtleBorder)
                ) {
                    Text(
                        text = "${task.qualityLabel} • ${task.formatExt.uppercase()} • ${FormatUtils.formatBytes(task.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.weight(0.6f))

                // Seek Slider
                val safeDuration = durationMs.coerceAtLeast(1)
                Slider(
                    value = positionMs.coerceIn(0, safeDuration).toFloat(),
                    onValueChange = { newVal ->
                        isDraggingSlider = true
                        positionMs = newVal.toInt()
                    },
                    onValueChangeFinished = {
                        runCatching { mediaPlayer?.seekTo(positionMs) }
                        isDraggingSlider = false
                    },
                    valueRange = 0f..safeDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = SnapPurple,
                        activeTrackColor = SnapPurple,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FormatUtils.formatDuration((positionMs / 1000L).coerceAtLeast(0L)).ifBlank { "00:00" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatDuration((durationMs / 1000L).coerceAtLeast(0L)).ifBlank { "00:00" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isLooping = !isLooping
                            runCatching { mediaPlayer?.isLooping = isLooping }
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Repeat,
                            contentDescription = stringResource(if (isLooping) R.string.player_loop_on else R.string.player_loop_off),
                            tint = if (isLooping) SnapPurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val target = (positionMs - 10_000).coerceAtLeast(0)
                            positionMs = target
                            runCatching { mediaPlayer?.seekTo(target) }
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Replay10,
                            contentDescription = stringResource(R.string.player_rewind_10),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SnapPurple, SnapBlue)))
                            .clickable(enabled = isPrepared) {
                                val mp = mediaPlayer ?: return@clickable
                                if (isPlaying) {
                                    mp.pause()
                                    isPlaying = false
                                } else {
                                    if (positionMs >= durationMs && durationMs > 0) {
                                        mp.seekTo(0)
                                        positionMs = 0
                                    }
                                    mp.start()
                                    isPlaying = true
                                }
                            }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isPrepared) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) SnapIcons.Pause else Icons.Filled.PlayArrow,
                                contentDescription = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val target = (positionMs + 10_000).coerceAtMost(durationMs)
                            positionMs = target
                            runCatching { mediaPlayer?.seekTo(target) }
                        }
                    ) {
                        Icon(
                            imageVector = SnapIcons.Forward10,
                            contentDescription = stringResource(R.string.player_forward_10),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(onClick = onOpenExternal) {
                        Icon(
                            imageVector = SnapIcons.OpenInNew,
                            contentDescription = stringResource(R.string.action_open_external),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlayerErrorOverlay(
    onOpenExternal: () -> Unit,
    onBack: () -> Unit,
    darkSurface: Boolean
) {
    val textColor = if (darkSurface) Color.White else MaterialTheme.colorScheme.onBackground
    val subColor = if (darkSurface) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = BrandGoldPrimary,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = stringResource(R.string.player_error_title),
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.player_error_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = subColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        GradientPrimaryButton(
            text = stringResource(R.string.action_open_external),
            icon = SnapIcons.OpenInNew,
            onClick = onOpenExternal,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(R.string.action_close), color = textColor)
        }
    }
}
