package com.prism.gleam.data

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.service.quicksettings.TileService

object GleamPreferences {
    private const val PREFS_NAME = "gleam_settings"
    const val ACTION_TOGGLE_POWER = "com.prism.gleam.ACTION_TOGGLE_POWER"

    private const val KEY_POWERED_ON = "is_powered_on"
    private const val KEY_AUTO_BRIGHTNESS = "is_auto_brightness"
    private const val KEY_RESTORE_MODE = "restore_mode"
    private const val KEY_PREVIOUS_VALUE = "previous_value"
    private const val KEY_CUSTOM_FLOOR = "custom_floor_value"
    private const val KEY_MIN_BRIGHTNESS = "min_brightness_percent"
    private const val KEY_MAX_BRIGHTNESS = "max_brightness_percent"
    private const val KEY_HAPTIC_FEEDBACK = "is_haptic_feedback_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Master Power State (The single pointer for App, Tile, and Shortcut)
    fun isPoweredOn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_POWERED_ON, true)
    }

    fun setPoweredOn(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_POWERED_ON, enabled).apply()
    }

    fun togglePoweredOn(context: Context): Boolean {
        val next = !isPoweredOn(context)
        setPoweredOn(context, next)
        return next
    }

    // Auto Brightness Toggle
    fun isAutoBrightness(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_BRIGHTNESS, true)
    }

    fun setAutoBrightness(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_BRIGHTNESS, enabled).apply()
    }

    fun toggleAutoBrightness(context: Context): Boolean {
        val next = !isAutoBrightness(context)
        setAutoBrightness(context, next)
        return next
    }

    // Restore Settings
    fun getRestoreMode(context: Context): String {
        return getPrefs(context).getString(KEY_RESTORE_MODE, "PREVIOUS") ?: "PREVIOUS"
    }

    fun setRestoreMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_RESTORE_MODE, mode).apply()
    }

    fun getPreviousValue(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREVIOUS_VALUE, 59)
    }

    fun setPreviousValue(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_PREVIOUS_VALUE, value).apply()
    }

    fun getCustomFloorValue(context: Context): Int {
        return getPrefs(context).getInt(KEY_CUSTOM_FLOOR, 50)
    }

    fun setCustomFloorValue(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_CUSTOM_FLOOR, value).apply()
    }

    // Dynamic Range Min & Max (Defaults: min 10%, max 100%)
    fun getMinBrightness(context: Context): Int {
        return getPrefs(context).getInt(KEY_MIN_BRIGHTNESS, 10)
    }

    fun setMinBrightness(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_MIN_BRIGHTNESS, value.coerceIn(0, 100)).apply()
    }

    fun getMaxBrightness(context: Context): Int {
        return getPrefs(context).getInt(KEY_MAX_BRIGHTNESS, 100)
    }

    fun setMaxBrightness(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_MAX_BRIGHTNESS, value.coerceIn(0, 100)).apply()
    }

    // Vibration / Haptic Feedback Toggle (Default: true)
    fun isHapticFeedback(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAPTIC_FEEDBACK, true)
    }

    fun setHapticFeedback(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    // Notification Shade Quick Tile Request Listening State
    fun requestTileUpdate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val tileComponent = ComponentName("com.prism.gleam", "com.prism.gleam.service.GleamTileService")
                TileService.requestListeningState(context.applicationContext, tileComponent)
            }
        } catch (_: Exception) {
            // Service not bound or running on older API
        }
    }
}
