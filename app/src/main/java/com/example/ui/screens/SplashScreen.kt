package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SnapTubeLogoMark
import com.example.ui.theme.BrandCoralRed
import com.example.ui.theme.LocalSnapTubeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val extended = LocalSnapTubeColors.current
    val logoScale = remember { Animatable(0.72f) }
    val contentAlpha = remember { Animatable(0f) }

    val pulseTransition = rememberInfiniteTransition(label = "splash_loader")
    val shimmerOffset by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loader_offset"
    )

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360)
            )
        }
        delay(1050L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(extended.splashGradient)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Warm luminous sunlight glow behind the SnapTube hexagon emblem
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.42f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.minDimension * 0.56f
                ),
                radius = size.minDimension * 0.56f,
                center = Offset(size.width * 0.5f, size.height * 0.42f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandCoralRed.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.80f, size.height * 0.78f),
                    radius = size.minDimension * 0.45f
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width * 0.80f, size.height * 0.78f)
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SnapTubeLogoMark(
                size = 112.dp,
                withContainer = true,
                modifier = Modifier.scale(logoScale.value)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1205),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF3E2A08).copy(alpha = 0.86f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Smooth Coral-Red to White loading indicator pill
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF3E2A08).copy(alpha = 0.16f))
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(4.5.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = (shimmerOffset * 64f).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandCoralRed, Color(0xFFFF6B57), Color.White)
                            )
                        )
                )
            }
        }
    }
}
