package com.prism.gleam.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetPreferences {
    private const val PREFS_NAME = "gleam_widget_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadConfig(context: Context, widgetId: Int): WidgetConfig {
        val prefs = getPrefs(context)
        val themeStr = prefs.getString("theme_$widgetId", WidgetTheme.SYSTEM.name) ?: WidgetTheme.SYSTEM.name
        val colorStyleStr = prefs.getString("color_style_$widgetId", WidgetColorStyle.PLAIN.name) ?: WidgetColorStyle.PLAIN.name
        val opacity = prefs.getInt("opacity_$widgetId", 100)
        val radius = prefs.getInt("radius_$widgetId", 30)
        val smoothing = prefs.getInt("smoothing_$widgetId", 0)

        val theme = try {
            WidgetTheme.valueOf(themeStr)
        } catch (_: Exception) {
            WidgetTheme.SYSTEM
        }

        val colorStyle = try {
            WidgetColorStyle.valueOf(colorStyleStr)
        } catch (_: Exception) {
            WidgetColorStyle.MINIMAL_GRADIENT
        }

        return WidgetConfig(
            theme = theme,
            colorStyle = colorStyle,
            opacityPercent = opacity,
            radiusDp = radius,
            smoothingPercent = smoothing
        )
    }

    fun saveConfig(context: Context, widgetId: Int, config: WidgetConfig) {
        getPrefs(context).edit()
            .putString("theme_$widgetId", config.theme.name)
            .putString("color_style_$widgetId", config.colorStyle.name)
            .putInt("opacity_$widgetId", config.opacityPercent)
            .putInt("radius_$widgetId", config.radiusDp)
            .putInt("smoothing_$widgetId", config.smoothingPercent)
            .apply()
    }

    fun deleteConfig(context: Context, widgetId: Int) {
        getPrefs(context).edit()
            .remove("theme_$widgetId")
            .remove("color_style_$widgetId")
            .remove("opacity_$widgetId")
            .remove("radius_$widgetId")
            .remove("smoothing_$widgetId")
            .apply()
    }
}
