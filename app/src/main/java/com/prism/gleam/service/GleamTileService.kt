package com.prism.gleam.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.prism.gleam.MainActivity
import com.prism.gleam.R
import com.prism.gleam.data.GleamPreferences
import com.prism.gleam.ui.components.canWriteSystemSettings

class GleamTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (!canWriteSystemSettings(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        // Toggle state
        val isNowOn = GleamPreferences.togglePoweredOn(this)
        updateTileState()

        // Notify app if running
        sendBroadcast(
            Intent(GleamPreferences.ACTION_TOGGLE_POWER).setPackage(packageName)
        )
    }

    private fun updateTileState() {
        val qsTile = qsTile ?: return
        val isEnabled = GleamPreferences.isPoweredOn(this)

        qsTile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.label = "Gleam Sun"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = if (isEnabled) "Boost ON" else "Standby"
        }
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_sun_boost)
        qsTile.updateTile()
    }
}
