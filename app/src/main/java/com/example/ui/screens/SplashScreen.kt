package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.SnapTubeLogoMark
import com.example.ui.theme.BrandGoldPrimary
import com.example.ui.theme.LocalSnapTubeColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 260)
        )
        delay(650L)
        onFinished()
    }

    val backgroundColor = if (extended.isDark) Color(0xFF0E1117) else Color.White
    val titleColor = if (extended.isDark) Color.White else Color(0xFF1A1205)
    val subtitleColor = if (extended.isDark) Color(0xFFA0A8B8) else Color(0xFF5C5549)
    val watermarkColor = if (extended.isDark) Color(0xFF8B93A4) else Color(0xFF8A8175)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(backgroundColor)
            .safeDrawingPadding()
    ) {
        // Subtle, completely static ambient golden glow behind the centered logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrandGoldPrimary.copy(alpha = if (extended.isDark) 0.14f else 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.44f),
                    radius = size.minDimension * 0.52f
                ),
                radius = size.minDimension * 0.52f,
                center = Offset(size.width * 0.5f, size.height * 0.44f)
            )
        }

        // Crisp static-centered SnapTube logo + name + tagline
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SnapTubeLogoMark(
                size = 104.dp,
                withContainer = true
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = titleColor,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }

        // Bottom brand watermark: Webtecit Apps
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(contentAlpha.value)
                .testTag("splash_watermark"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.splash_watermark),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp
                ),
                color = watermarkColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
