package com.example.fizyoapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.fizyoapp.presentation.ui.theme.*


private val LightColors = lightColorScheme(
    primary = primaryColor,
    onPrimary = surfaceColor,
    primaryContainer = primaryColor.copy(alpha = 0.8f),
    onPrimaryContainer = surfaceColor,
    secondary = accentColor,
    onSecondary = surfaceColor,
    secondaryContainer = accentColor.copy(alpha = 0.8f),
    onSecondaryContainer = surfaceColor,
    tertiary = infoColor,
    onTertiary = surfaceColor,
    tertiaryContainer = infoColor.copy(alpha = 0.8f),
    onTertiaryContainer = surfaceColor,
    error = errorColor,
    errorContainer = errorColor.copy(alpha = 0.1f),
    onError = surfaceColor,
    onErrorContainer = errorColor,
    background = backgroundColor,
    onBackground = textColor,
    surface = surfaceColor,
    onSurface = textColor,
    surfaceVariant = backgroundColor.copy(alpha = 0.7f),
    onSurfaceVariant = textColor,
    outline = cardBorderColor,
    inverseOnSurface = surfaceColor,
    inverseSurface = textColor,
    inversePrimary = surfaceColor,
    surfaceTint = primaryColor,
    outlineVariant = cardBorderColor.copy(alpha = 0.5f),
    scrim = overlayColor,
)

private val DarkColors = darkColorScheme(
    primary = primaryColor,
    onPrimary = surfaceColor,
    primaryContainer = primaryColor.copy(alpha = 0.8f),
    onPrimaryContainer = surfaceColor,
    secondary = accentColor,
    onSecondary = surfaceColor,
    secondaryContainer = accentColor.copy(alpha = 0.8f),
    onSecondaryContainer = surfaceColor,
    tertiary = infoColor,
    onTertiary = surfaceColor,
    tertiaryContainer = infoColor.copy(alpha = 0.8f),
    onTertiaryContainer = surfaceColor,
    error = errorColor,
    errorContainer = errorColor.copy(alpha = 0.1f),
    onError = surfaceColor,
    onErrorContainer = errorColor,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF8A8A8A),
    inverseOnSurface = Color(0xFF1E1E1E),
    inverseSurface = Color(0xFFE0E0E0),
    inversePrimary = surfaceColor,
    surfaceTint = primaryColor,
    outlineVariant = Color(0xFF444444),
    scrim = overlayColor,
)

@Composable
fun FizyoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}