package com.fyllo.filemanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.fyllo.filemanager.ui.screens.SettingsState

private val DefaultDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = SoftBackground,
    surface = SoftSurface,
    surfaceVariant = Color(0xFF202428),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC4C7C5)
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF444746)
)

// Purple Theme
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C27B0),
    secondary = Color(0xFFE1BEE7),
    tertiary = Color(0xFFCE93D8),
    background = Color(0xFF12001A),
    surface = Color(0xFF2A003D),
    surfaceVariant = Color(0xFF3F0A59),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFE1BEE7)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF9C27B0),
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFFCE93D8),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF4A148C)
)

// Pink Theme
private val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE91E63),
    secondary = Color(0xFFF8BBD0),
    tertiary = Color(0xFFF48FB1),
    background = Color(0xFF2A0010),
    surface = Color(0xFF3F0018),
    surfaceVariant = Color(0xFF5E0626),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFF8BBD0)
)

private val PinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),
    secondary = Color(0xFFE91E63),
    tertiary = Color(0xFFF48FB1),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF880E4F)
)

// Ocean Blue Theme
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF0288D1),
    secondary = Color(0xFF80DEEA),
    tertiary = Color(0xFF4DD0E1),
    background = Color(0xFF001F2D),
    surface = Color(0xFF00334E),
    surfaceVariant = Color(0xFF004D73),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB2EBF2)
)

private val OceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    secondary = Color(0xFF0288D1),
    tertiary = Color(0xFF4DD0E1),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF006064)
)

// Forest Green Theme
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFFA5D6A7),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF0A1F0C),
    surface = Color(0xFF143818),
    surfaceVariant = Color(0xFF1E5223),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFA5D6A7)
)

private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF2E7D32),
    tertiary = Color(0xFF81C784),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF1B5E20)
)

// E-ink Theme
val LocalEInkMode = compositionLocalOf { false }

private val EInkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE7EBE0),
    secondary = Color(0xFF6B6B6B),
    tertiary = Color(0xFF888888),
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF2C2C2E),
    onPrimary = Color(0xFF1C1C1E),
    onSecondary = Color(0xFFE7EBE0),
    onTertiary = Color(0xFFE7EBE0),
    onBackground = Color(0xFFE7EBE0),
    onSurface = Color(0xFFE7EBE0),
    onSurfaceVariant = Color(0xFFE7EBE0),
    outline = Color(0xFFE7EBE0)
)

private val EInkLightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1C1E),
    secondary = Color(0xFF6B6B6B),
    tertiary = Color(0xFF444444),
    background = Color(0xFFE7EBE0),
    surface = Color(0xFFE7EBE0),
    surfaceVariant = Color(0xFFD7DBD0),
    onPrimary = Color(0xFFE7EBE0),
    onSecondary = Color(0xFF1C1C1E),
    onTertiary = Color(0xFF1C1C1E),
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF1C1C1E),
    outline = Color(0xFF1C1C1E)
)

private val EInkTypography = Typography(
    displayLarge = Typography.displayLarge.copy(fontFamily = FontFamily.Monospace),
    displayMedium = Typography.displayMedium.copy(fontFamily = FontFamily.Monospace),
    displaySmall = Typography.displaySmall.copy(fontFamily = FontFamily.Monospace),
    headlineLarge = Typography.headlineLarge.copy(fontFamily = FontFamily.Monospace),
    headlineMedium = Typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
    headlineSmall = Typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
    titleLarge = Typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
    titleMedium = Typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
    titleSmall = Typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
    bodyLarge = Typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
    bodyMedium = Typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    bodySmall = Typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    labelLarge = Typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
    labelMedium = Typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
    labelSmall = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
)

@Composable
fun SFileManagerTheme(
    settingsState: SettingsState,
    content: @Composable () -> Unit
) {
    val darkTheme = when (settingsState.themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme() // "System Default"
    }

    var colorScheme = when (settingsState.colorTheme) {
        "Purple" -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        "Pink" -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        "Ocean Blue" -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        "Forest Green" -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        "E-ink" -> if (darkTheme) EInkDarkColorScheme else EInkLightColorScheme
        else -> if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
    }

    val isEInk = settingsState.eInkMode || settingsState.colorTheme == "E-ink"
    
    if (isEInk) {
        colorScheme = if (darkTheme) EInkDarkColorScheme else EInkLightColorScheme
    }

    if (settingsState.amoledBlack && darkTheme && !isEInk) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalEInkMode provides isEInk) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (isEInk) EInkTypography else Typography,
            content = content
        )
    }
}
