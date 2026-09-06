package com.local.hyperoswhitelistkeeper.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.local.hyperoswhitelistkeeper.data.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = XiaomiOrange,
    onPrimary = Color(0xFF241208),
    primaryContainer = OrangeContainerLight,
    onPrimaryContainer = Color(0xFF351000),
    background = LightBackground,
    onBackground = Color(0xFF1B1B1B),
    surface = LightSurface,
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFFF0F0F2),
    onSurfaceVariant = Color(0xFF5F5F65),
    outline = Color(0xFF77777E),
)

private val DarkColorScheme = darkColorScheme(
    primary = XiaomiOrange,
    onPrimary = Color(0xFF241208),
    primaryContainer = OrangeContainerDark,
    onPrimaryContainer = Color(0xFFFFDBCA),
    background = DarkBackground,
    onBackground = Color(0xFFF1F1F1),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF29292C),
    onSurfaceVariant = Color(0xFFC7C5CA),
    outline = Color(0xFF918F95),
)

@Composable
fun HyperOSWhitelistKeeperTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
