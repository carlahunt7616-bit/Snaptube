package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.PlatformType
import com.example.ui.theme.BrandCoralDeep
import com.example.ui.theme.BrandCoralRed
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandGoldPrimary
import com.example.ui.theme.LocalSnapTubeColors
import com.example.ui.theme.SnapBlue
import com.example.ui.theme.SnapCyan
import com.example.ui.theme.SnapPurple
import com.example.ui.theme.SnapPurpleDeep

/**
 * Original SnapTube emblem in signature Golden Yellow, Pure White, and Coral Red:
 * a crisp rounded hexagonal white badge holding a segmented coral-red download arrow
 * with a centered play triangle cutout.
 */
@Composable
fun SnapTubeLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    withContainer: Boolean = true
) {
    val cd = stringResource(R.string.cd_brand_logo)
    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            if (withContainer) {
                val radius = w * 0.28f
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(BrandGoldLight, BrandGoldPrimary, Color(0xFFF7A600)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    cornerRadius = CornerRadius(radius, radius)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = (w * 0.024f).coerceAtLeast(1f))
                )
            }

            // Soft 3D elevation shadow beneath white hexagon badge
            val hexShadow = buildRoundedHexagonPath(w, h, offsetY = h * 0.032f)
            drawPath(path = hexShadow, color = Color(0x33A85A00))

            // Glossy pure white geometric hexagon badge
            val hexPath = buildRoundedHexagonPath(w, h, offsetY = 0f)
            drawPath(
                path = hexPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF6F7FA)),
                    startY = h * 0.14f,
                    endY = h * 0.86f
                )
            )
            drawPath(
                path = hexPath,
                color = Color(0x14000000),
                style = Stroke(width = (w * 0.014f).coerceAtLeast(0.8f))
            )

            // Coral Red gradient brush for speed bars + download arrow
            val coralBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF6154), BrandCoralRed, BrandCoralDeep),
                startY = h * 0.28f,
                endY = h * 0.74f
            )

            // Top sleek speed bar 1
            drawRoundRect(
                brush = coralBrush,
                topLeft = Offset(w * 0.415f, h * 0.315f),
                size = Size(w * 0.17f, h * 0.042f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )

            // Speed bar 2
            drawRoundRect(
                brush = coralBrush,
                topLeft = Offset(w * 0.415f, h * 0.382f),
                size = Size(w * 0.17f, h * 0.046f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )

            // Main downward download arrow body + head
            val arrowPath = Path().apply {
                moveTo(w * 0.415f, h * 0.452f)
                lineTo(w * 0.585f, h * 0.452f)
                lineTo(w * 0.585f, h * 0.545f)
                lineTo(w * 0.665f, h * 0.545f)
                lineTo(w * 0.50f, h * 0.725f)
                lineTo(w * 0.335f, h * 0.545f)
                lineTo(w * 0.415f, h * 0.545f)
                close()
            }
            drawPath(path = arrowPath, brush = coralBrush)

            // Crisp white Play Triangle centered inside the arrow head
            val playPath = Path().apply {
                moveTo(w * 0.472f, h * 0.548f)
                lineTo(w * 0.556f, h * 0.594f)
                lineTo(w * 0.472f, h * 0.640f)
                close()
            }
            drawPath(path = playPath, color = Color.White)
        }
    }
}

private fun buildRoundedHexagonPath(w: Float, h: Float, offsetY: Float): Path {
    val cx = w * 0.50f
    val cy = h * 0.50f + offsetY
    val r = w * 0.355f
    val dx = r * 0.866f
    val dy = r * 0.50f

    val pts = listOf(
        Offset(cx, cy - r),
        Offset(cx + dx, cy - dy),
        Offset(cx + dx, cy + dy),
        Offset(cx, cy + r),
        Offset(cx - dx, cy + dy),
        Offset(cx - dx, cy - dy)
    )

    val cornerFrac = 0.22f
    return Path().apply {
        for (i in pts.indices) {
            val prev = pts[(i - 1 + pts.size) % pts.size]
            val curr = pts[i]
            val next = pts[(i + 1) % pts.size]

            val startX = curr.x + (prev.x - curr.x) * cornerFrac
            val startY = curr.y + (prev.y - curr.y) * cornerFrac
            val endX = curr.x + (next.x - curr.x) * cornerFrac
            val endY = curr.y + (next.y - curr.y) * cornerFrac

            if (i == 0) moveTo(startX, startY) else lineTo(startX, startY)
            quadraticTo(curr.x, curr.y, endX, endY)
        }
        close()
    }
}

/**
 * Recognizable, custom-drawn vector brand icons for each supported platform
 * (YouTube, Facebook, Instagram, TikTok, X, Pinterest, Reddit, Vimeo, Dailymotion, Direct URL).
 */
@Composable
fun PlatformLogoIcon(
    platform: PlatformType,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            when (platform) {
                PlatformType.YOUTUBE -> drawYouTubeLogo(w, h)
                PlatformType.FACEBOOK -> drawFacebookLogo(w, h)
                PlatformType.INSTAGRAM -> drawInstagramLogo(w, h)
                PlatformType.TIKTOK -> drawTikTokLogo(w, h)
                PlatformType.X_TWITTER -> drawXLogo(w, h)
                PlatformType.PINTEREST -> drawPinterestLogo(w, h)
                PlatformType.REDDIT -> drawRedditLogo(w, h)
                PlatformType.VIMEO -> drawVimeoLogo(w, h)
                PlatformType.DAILYMOTION -> drawDailymotionLogo(w, h)
                PlatformType.DIRECT -> drawDirectUrlLogo(w, h)
            }
        }
    }
}

private fun DrawScope.drawYouTubeLogo(w: Float, h: Float) {
    // Classic red rounded-rectangle YouTube badge + crisp white play button
    val bgTop = h * 0.14f
    val bgHeight = h * 0.72f
    drawRoundRect(
        color = Color(0xFFFF0000),
        topLeft = Offset(w * 0.04f, bgTop),
        size = Size(w * 0.92f, bgHeight),
        cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
    )
    val play = Path().apply {
        moveTo(w * 0.41f, h * 0.35f)
        lineTo(w * 0.67f, h * 0.50f)
        lineTo(w * 0.41f, h * 0.65f)
        close()
    }
    drawPath(play, color = Color.White)
}

private fun DrawScope.drawFacebookLogo(w: Float, h: Float) {
    // Classic Facebook Blue circle + bold white lowercase 'f'
    drawCircle(color = Color(0xFF1877F2), radius = w * 0.48f, center = Offset(w * 0.5f, h * 0.5f))
    val fPath = Path().apply {
        moveTo(w * 0.56f, h * 0.97f)
        lineTo(w * 0.56f, h * 0.61f)
        lineTo(w * 0.68f, h * 0.61f)
        lineTo(w * 0.705f, h * 0.48f)
        lineTo(w * 0.56f, h * 0.48f)
        lineTo(w * 0.56f, h * 0.40f)
        quadraticTo(w * 0.56f, h * 0.34f, w * 0.63f, h * 0.34f)
        lineTo(w * 0.71f, h * 0.34f)
        lineTo(w * 0.71f, h * 0.22f)
        quadraticTo(w * 0.64f, h * 0.21f, w * 0.58f, h * 0.21f)
        quadraticTo(w * 0.44f, h * 0.21f, w * 0.44f, h * 0.38f)
        lineTo(w * 0.44f, h * 0.48f)
        lineTo(w * 0.33f, h * 0.48f)
        lineTo(w * 0.33f, h * 0.61f)
        lineTo(w * 0.44f, h * 0.61f)
        lineTo(w * 0.44f, h * 0.97f)
        close()
    }
    drawPath(fPath, color = Color.White)
}

private fun DrawScope.drawInstagramLogo(w: Float, h: Float) {
    // Multi-stop Instagram gradient rounded square + white camera frame, lens, dot
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF515BD4), Color(0xFF8134AF), Color(0xFFDD2A7B), Color(0xFFFEDA77)),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        ),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    val strokeW = (w * 0.075f).coerceAtLeast(1.8f)
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.23f, h * 0.23f),
        size = Size(w * 0.54f, h * 0.54f),
        cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
        style = Stroke(width = strokeW)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.145f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = strokeW)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.045f,
        center = Offset(w * 0.655f, h * 0.345f)
    )
}

private fun DrawScope.drawTikTokLogo(w: Float, h: Float) {
    // Deep black rounded badge + iconic Cyan/Pink/White offset musical note
    drawRoundRect(
        color = Color(0xFF121216),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    fun drawNote(dx: Float, dy: Float, color: Color) {
        val sw = (w * 0.10f).coerceAtLeast(2.2f)
        // Curved bottom loop
        drawCircle(
            color = color,
            radius = w * 0.14f,
            center = Offset(w * 0.43f + dx, h * 0.65f + dy),
            style = Stroke(width = sw)
        )
        // Vertical stem + top wave flag
        val stem = Path().apply {
            moveTo(w * 0.57f + dx, h * 0.65f + dy)
            lineTo(w * 0.57f + dx, h * 0.25f + dy)
            quadraticTo(w * 0.60f + dx, h * 0.38f + dy, w * 0.75f + dx, h * 0.39f + dy)
        }
        drawPath(
            path = stem,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
    drawNote(-w * 0.032f, -h * 0.022f, Color(0xFF25F4EE))
    drawNote(w * 0.032f, h * 0.022f, Color(0xFFFE2C55))
    drawNote(0f, 0f, Color.White)
}

private fun DrawScope.drawXLogo(w: Float, h: Float) {
    // Bold jet black badge + crisp geometric white X mark
    drawRoundRect(
        color = Color(0xFF0F1419),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    val leftBar = Path().apply {
        moveTo(w * 0.24f, h * 0.23f)
        lineTo(w * 0.38f, h * 0.23f)
        lineTo(w * 0.76f, h * 0.77f)
        lineTo(w * 0.62f, h * 0.77f)
        close()
    }
    drawPath(leftBar, color = Color.White)
    drawLine(
        color = Color.White,
        start = Offset(w * 0.72f, h * 0.24f),
        end = Offset(w * 0.28f, h * 0.76f),
        strokeWidth = (w * 0.085f).coerceAtLeast(2f),
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawPinterestLogo(w: Float, h: Float) {
    // Signature Pinterest red circle + clean stylized white 'P' pin
    drawCircle(color = Color(0xFFE60023), radius = w * 0.48f, center = Offset(w * 0.5f, h * 0.5f))
    val sw = (w * 0.095f).coerceAtLeast(2f)
    // Pin stem angled gently like Pinterest 'P'
    drawLine(
        color = Color.White,
        start = Offset(w * 0.47f, h * 0.44f),
        end = Offset(w * 0.39f, h * 0.80f),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
    val loop = Path().apply {
        moveTo(w * 0.35f, h * 0.56f)
        cubicTo(w * 0.26f, h * 0.36f, w * 0.42f, h * 0.22f, w * 0.57f, h * 0.24f)
        cubicTo(w * 0.73f, h * 0.26f, w * 0.74f, h * 0.48f, w * 0.62f, h * 0.58f)
        cubicTo(w * 0.54f, h * 0.64f, w * 0.46f, h * 0.59f, w * 0.45f, h * 0.53f)
    }
    drawPath(loop, color = Color.White, style = Stroke(width = sw, cap = StrokeCap.Round))
}

private fun DrawScope.drawRedditLogo(w: Float, h: Float) {
    // Reddit orange circle + white Snoo head, ears, antenna & orange eyes
    val orange = Color(0xFFFF4500)
    drawCircle(color = orange, radius = w * 0.48f, center = Offset(w * 0.5f, h * 0.5f))
    // Antenna
    drawLine(
        color = Color.White,
        start = Offset(w * 0.50f, h * 0.38f),
        end = Offset(w * 0.56f, h * 0.22f),
        strokeWidth = (w * 0.055f).coerceAtLeast(1.5f),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.56f, h * 0.22f),
        end = Offset(w * 0.68f, h * 0.26f),
        strokeWidth = (w * 0.055f).coerceAtLeast(1.5f),
        cap = StrokeCap.Round
    )
    drawCircle(color = Color.White, radius = w * 0.06f, center = Offset(w * 0.71f, h * 0.27f))
    // Ears
    drawCircle(color = Color.White, radius = w * 0.085f, center = Offset(w * 0.24f, h * 0.52f))
    drawCircle(color = Color.White, radius = w * 0.085f, center = Offset(w * 0.76f, h * 0.52f))
    // Oval Head
    drawOval(
        color = Color.White,
        topLeft = Offset(w * 0.24f, h * 0.37f),
        size = Size(w * 0.52f, h * 0.38f)
    )
    // Eyes
    drawCircle(color = orange, radius = w * 0.052f, center = Offset(w * 0.40f, h * 0.53f))
    drawCircle(color = orange, radius = w * 0.052f, center = Offset(w * 0.60f, h * 0.53f))
    // Smile
    val smile = Path().apply {
        moveTo(w * 0.40f, h * 0.64f)
        quadraticTo(w * 0.50f, h * 0.71f, w * 0.60f, h * 0.64f)
    }
    drawPath(smile, color = orange, style = Stroke(width = (w * 0.045f).coerceAtLeast(1.3f), cap = StrokeCap.Round))
}

private fun DrawScope.drawVimeoLogo(w: Float, h: Float) {
    // Vimeo Cyan rounded square + iconic swooping white 'v'
    drawRoundRect(
        color = Color(0xFF1AB7EA),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    val vPath = Path().apply {
        moveTo(w * 0.23f, h * 0.42f)
        quadraticTo(w * 0.34f, h * 0.28f, w * 0.41f, h * 0.44f)
        lineTo(w * 0.47f, h * 0.64f)
        quadraticTo(w * 0.52f, h * 0.75f, w * 0.60f, h * 0.63f)
        quadraticTo(w * 0.74f, h * 0.43f, w * 0.73f, h * 0.31f)
        quadraticTo(w * 0.71f, h * 0.23f, w * 0.62f, h * 0.31f)
    }
    drawPath(
        path = vPath,
        color = Color.White,
        style = Stroke(width = (w * 0.115f).coerceAtLeast(2.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawDailymotionLogo(w: Float, h: Float) {
    // Dailymotion Deep Blue rounded square + crisp bold lowercase geometric 'd'
    drawRoundRect(
        color = Color(0xFF0066DC),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    val sw = (w * 0.115f).coerceAtLeast(2.4f)
    drawCircle(
        color = Color.White,
        radius = w * 0.165f,
        center = Offset(w * 0.47f, h * 0.60f),
        style = Stroke(width = sw)
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.635f, h * 0.23f),
        end = Offset(w * 0.635f, h * 0.75f),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDirectUrlLogo(w: Float, h: Float) {
    // Vibrant Purple-Blue-Cyan gradient badge + clean chain link icon
    drawRoundRect(
        brush = Brush.linearGradient(listOf(SnapPurple, SnapBlue, SnapCyan)),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.92f),
        cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
    )
    val sw = (w * 0.085f).coerceAtLeast(2f)
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.22f, h * 0.41f),
        size = Size(w * 0.30f, h * 0.18f),
        cornerRadius = CornerRadius(h * 0.09f, h * 0.09f),
        style = Stroke(width = sw)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.48f, h * 0.41f),
        size = Size(w * 0.30f, h * 0.18f),
        cornerRadius = CornerRadius(h * 0.09f, h * 0.09f),
        style = Stroke(width = sw)
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.41f, h * 0.50f),
        end = Offset(w * 0.59f, h * 0.50f),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
}

/**
 * Symmetrical, evenly spaced tile for the 5x2 Platform Recognition grid on HomeScreen.
 */
@Composable
fun PlatformGridTile(
    platform: PlatformType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val label = when (platform) {
        PlatformType.X_TWITTER -> "X"
        PlatformType.DAILYMOTION -> "Dailymotion"
        PlatformType.DIRECT -> "Direct URL"
        else -> platform.displayName
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PlatformLogoIcon(
            platform = platform,
            size = 38.dp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    testTag: String = ""
) {
    val extended = LocalSnapTubeColors.current
    val shape = RoundedCornerShape(16.dp)
    val bgBrush = if (enabled) {
        extended.brandGradient
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    val contentColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .defaultMinSize(minHeight = 52.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = shape,
                ambientColor = SnapPurple.copy(alpha = 0.25f),
                spotColor = SnapBlue.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(bgBrush)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(PaddingValues(horizontal = 20.dp, vertical = 14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.2.dp
                )
                Spacer(Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@Composable
fun PlatformBadge(
    platform: PlatformType,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accent = Color(platform.accentHex)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PlatformLogoIcon(
                platform = platform,
                size = if (compact) 18.dp else 20.dp
            )
            Text(
                text = platform.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MediaThumbnailBox(
    thumbnailUrl: String?,
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fallbackBrush = if (mediaType == MediaType.AUDIO) {
        Brush.linearGradient(listOf(SnapPurpleDeep, SnapPurple, Color(0xFF9333EA)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF1E1B4B), SnapBlue, SnapCyan))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fallbackBrush)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (mediaType == MediaType.AUDIO) SnapIcons.MusicNote else SnapIcons.VideoFile,
            contentDescription = stringResource(R.string.cd_thumbnail),
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(28.dp)
        )
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = stringResource(R.string.cd_thumbnail),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
