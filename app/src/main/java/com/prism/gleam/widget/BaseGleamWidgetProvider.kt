package com.prism.gleam.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.prism.gleam.MainActivity
import com.prism.gleam.controller.BrightnessController
import com.prism.gleam.data.GleamPreferences
import com.prism.gleam.ui.components.RestoreMode
import com.prism.gleam.ui.components.canWriteSystemSettings

abstract class BaseGleamWidgetProvider(
    private val widgetSize: WidgetSize
) : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            GleamWidgetManager.updateWidget(context, appWidgetManager, id, widgetSize)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        GleamWidgetManager.updateWidget(context, appWidgetManager, appWidgetId, widgetSize)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            WidgetPreferences.deleteConfig(context, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            GleamWidgetManager.ACTION_WIDGET_TOGGLE -> {
                if (!canWriteSystemSettings(context)) {
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(mainIntent)
                    Toast.makeText(context, "Gleam requires 'Modify System Settings' permission", Toast.LENGTH_SHORT).show()
                    return
                }

                // Toggle master power state (single pointer sync)
                val isNowOn = GleamPreferences.togglePoweredOn(context)
                val toastMsg = if (isNowOn) "Gleam: Boost ON" else "Gleam: Standby"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()

                if (isNowOn) {
                    BrightnessController.triggerOneShotSunlightBoost(context)
                } else {
                    val restoreMode = try {
                        RestoreMode.valueOf(GleamPreferences.getRestoreMode(context))
                    } catch (_: Exception) {
                        RestoreMode.PREVIOUS
                    }
                    BrightnessController.restoreBrightness(
                        context,
                        null,
                        restoreMode,
                        GleamPreferences.getPreviousValue(context),
                        GleamPreferences.getCustomFloorValue(context)
                    )
                }

                // Notify Quick Settings Tile
                GleamPreferences.requestTileUpdate(context)

                // Notify open app
                context.sendBroadcast(
                    Intent(GleamPreferences.ACTION_TOGGLE_POWER).setPackage(context.packageName)
                )

                // Update all widgets immediately
                GleamWidgetManager.updateAllWidgets(context)
            }

            GleamPreferences.ACTION_TOGGLE_POWER -> {
                GleamWidgetManager.updateAllWidgets(context)
            }
        }
    }
}
