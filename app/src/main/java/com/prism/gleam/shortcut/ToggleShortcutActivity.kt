package com.prism.gleam.shortcut

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.prism.gleam.R
import com.prism.gleam.controller.BrightnessController
import com.prism.gleam.data.GleamPreferences
import com.prism.gleam.ui.components.RestoreMode
import com.prism.gleam.ui.components.canWriteSystemSettings

class ToggleShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!canWriteSystemSettings(this)) {
            Toast.makeText(this, "Gleam requires 'Modify System Settings' permission", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Master single pointer toggle (exact same state as Quick Tile and in-app ON/OFF button)
        val isNowOn = GleamPreferences.togglePoweredOn(this)
        val statusText = if (isNowOn) "Sunlight Boost: ON" else "Sunlight Boost: OFF"
        Toast.makeText(this, "Gleam: $statusText", Toast.LENGTH_SHORT).show()

        if (isNowOn) {
            BrightnessController.triggerOneShotSunlightBoost(this)
        } else {
            val restoreMode = try {
                RestoreMode.valueOf(GleamPreferences.getRestoreMode(this))
            } catch (_: Exception) {
                RestoreMode.PREVIOUS
            }
            BrightnessController.restoreBrightness(
                this,
                null,
                restoreMode,
                GleamPreferences.getPreviousValue(this),
                GleamPreferences.getCustomFloorValue(this)
            )
        }

        // Notify Quick Settings Tile to refresh state
        GleamPreferences.requestTileUpdate(this)

        // Notify MainActivity and widgets if running
        sendBroadcast(
            Intent(GleamPreferences.ACTION_TOGGLE_POWER).setPackage(packageName)
        )
        com.prism.gleam.widget.GleamWidgetManager.updateAllWidgets(this)

        finish()
    }

    companion object {
        const val SHORTCUT_ID = "gleam_toggle_sunlight_power"

        fun pinShortcutToHomeScreen(context: Context) {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                Toast.makeText(context, "Home screen pin shortcuts not supported on this launcher", Toast.LENGTH_SHORT).show()
                return
            }

            val targetIntent = Intent(context, ToggleShortcutActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pinShortcutInfo = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_toggle))
                .setShortLabel("Toggle Gleam")
                .setLongLabel("Toggle Sunlight Boost ON/OFF")
                .setIntent(targetIntent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
            Toast.makeText(context, "Shortcut added to home screen", Toast.LENGTH_SHORT).show()
        }
    }
}
