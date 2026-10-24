package com.prism.gleam.widget

import android.content.Context
import android.content.res.Configuration

enum class WidgetTheme(val label: String) {
    SYSTEM("Match System Setting"),
    LIGHT("Light"),
    DARK("Dark");

    fun isDark(context: Context): Boolean {
        return when (this) {
            SYSTEM -> {
                val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
            LIGHT -> false
            DARK -> true
        }
    }
}

enum class WidgetColorStyle(val label: String) {
    PLAIN("Plain"),
    MINIMAL_GRADIENT("Minimal Gradient")
}

data class WidgetConfig(
    val theme: WidgetTheme = WidgetTheme.SYSTEM,
    val colorStyle: WidgetColorStyle = WidgetColorStyle.PLAIN, // Default: Plain
    val opacityPercent: Int = 100, // 50, 70, 100
    val radiusDp: Int = 30,        // Default: 30 (Rounded)
    val smoothingPercent: Int = 0  // Default: 0 (None)
) {
    companion object {
        val OPACITY_STOPS = listOf(50, 70, 100)
        val RADIUS_STOPS = listOf(15, 30, 70)
        val SMOOTHING_STOPS = listOf(0, 50, 100)
    }
}
