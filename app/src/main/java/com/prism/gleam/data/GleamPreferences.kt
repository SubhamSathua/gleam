package com.prism.gleam.data

import android.content.Context
import android.content.SharedPreferences

object GleamPreferences {
    private const val PREFS_NAME = "gleam_settings"
    const val ACTION_TOGGLE_POWER = "com.prism.gleam.ACTION_TOGGLE_POWER"
    const val ACTION_TOGGLE_AUTO = "com.prism.gleam.ACTION_TOGGLE_AUTO"

    private const val KEY_POWERED_ON = "is_powered_on"
    private const val KEY_AUTO_BRIGHTNESS = "is_auto_brightness"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

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
}
