package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode

@Immutable
data class SnapTubeExtendedColors(
    val brandGradient: Brush,
    val splashGradient: Brush,
    val cardElevated: Color,
    val subtleBorder: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val cyanAccent: Color,
    val blueAccent: Color,
    val isDark: Boolean
)

val LocalSnapTubeColors = staticCompositionLocalOf {
    SnapTubeExtendedColors(
        brandGradient = Brush.linearGradient(listOf(SnapPurple, SnapBlue, SnapCyan)),
        splashGradient = Brush.linearGradient(listOf(SplashDeepStart, SplashMidIndigo, SplashBlueEdge, SplashCyanGlow)),
        cardElevated = LightSurfaceContainer,
        subtleBorder = LightOutline,
        success = EmeraldSuccess,
        successContainer = EmeraldSuccessContainerLight,
        warning = AmberWarning,
        warningContainer = AmberWarningContainerLight,
        cyanAccent = SnapCyan,
        blueAccent = SnapBlue,
        isDark = false
    )
}

private val SnapTubeLightColorScheme = lightColorScheme(
    primary = SnapPurple,
    onPrimary = Color.White,
    primaryContainer = SnapPurpleSoft,
    onPrimaryContainer = SnapPurpleDeep,
    secondary = SnapBlue,
    onSecondary = Color.White,
    secondaryContainer = SnapBlueSoft,
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = SnapCyan,
    onTertiary = Color.White,
    tertiaryContainer = SnapCyanSoft,
    onTertiaryContainer = Color(0xFF164E63),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = Color(0xFFEAEFF7),
    error = RoseError,
    onError = Color.White,
    errorContainer = RoseErrorContainerLight,
    onErrorContainer = Color(0xFF991B1B)
)

private val SnapTubeDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B62FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B1F5C),
    onPrimaryContainer = Color(0xFFDFD5FF),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0A1931),
    secondaryContainer = Color(0xFF1B2B4B),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF062831),
    tertiaryContainer = Color(0xFF123642),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF232838),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = RoseErrorContainerDark,
    onErrorContainer = Color(0xFFFECACA)
)

val SnapTubeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SnapTubeTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) SnapTubeDarkColorScheme else SnapTubeLightColorScheme
    val extendedColors = SnapTubeExtendedColors(
        brandGradient = Brush.linearGradient(listOf(BrandGoldLight, BrandGoldPrimary, Color(0xFFF5A600))),
        splashGradient = Brush.linearGradient(
            listOf(BrandGoldLight, BrandGoldPrimary, Color(0xFFFF9F1C))
        ),
        cardElevated = if (useDark) DarkSurfaceContainer else LightSurfaceContainer,
        subtleBorder = if (useDark) DarkOutline else LightOutline,
        success = EmeraldSuccess,
        successContainer = if (useDark) EmeraldSuccessContainerDark else EmeraldSuccessContainerLight,
        warning = AmberWarning,
        warningContainer = if (useDark) AmberWarningContainerDark else AmberWarningContainerLight,
        cyanAccent = SnapCyan,
        blueAccent = SnapBlue,
        isDark = useDark
    )

    CompositionLocalProvider(LocalSnapTubeColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SnapTubeTypography,
            shapes = SnapTubeShapes,
            content = content
        )
    }
}
