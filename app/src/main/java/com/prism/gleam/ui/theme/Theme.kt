package com.prism.gleam.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SolarOrange,
    secondary = SolarPeach,
    tertiary = SolarOrangeBright,
    background = PorcelainCanvas,
    surface = PorcelainCardWhite,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = TextTitleLight,
    onBackground = TextTitleLight,
    onSurface = TextTitleLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SolarOrangeDark,
    secondary = SolarPeachDark,
    tertiary = SolarOrangeDark,
    background = ObsidianCanvas,
    surface = ObsidianCard,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = TextTitleDark,
    onBackground = TextTitleDark,
    onSurface = TextTitleDark
)

object GleamTheme {
    val colors: GleamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGleamColors.current
}

@Composable
fun GleamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val gleamColors = if (darkTheme) DarkGleamColors else LightGleamColors
    val materialScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = gleamColors.canvas.toArgb()
                it.navigationBarColor = gleamColors.canvas.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGleamColors provides gleamColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
