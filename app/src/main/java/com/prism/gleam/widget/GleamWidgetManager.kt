package com.prism.gleam.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.prism.gleam.R
import com.prism.gleam.data.GleamPreferences
import kotlin.math.roundToInt

object GleamWidgetManager {

    const val ACTION_WIDGET_TOGGLE = "com.prism.gleam.ACTION_WIDGET_TOGGLE"

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        size: WidgetSize
    ) {
        val config = WidgetPreferences.loadConfig(context, widgetId)
        val isPoweredOn = GleamPreferences.isPoweredOn(context)

        val options: Bundle? = appWidgetManager.getAppWidgetOptions(widgetId)
        val density = context.resources.displayMetrics.density

        // Determine target pixel dimensions from widget options or fallback
        val defaultDp = when (size) {
            WidgetSize.SIZE_1X1 -> Pair(80, 80)
            WidgetSize.SIZE_2X2 -> Pair(160, 80)
            WidgetSize.SIZE_4X4 -> Pair(160, 160) // 2x2 1:1 square
        }

        val minWdp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaultDp.first) ?: defaultDp.first
        val minHdp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, defaultDp.second) ?: defaultDp.second

        val widthPx = (minWdp * density).roundToInt().coerceAtLeast(120)
        val heightPx = (minHdp * density).roundToInt().coerceAtLeast(120)

        val bitmap = GleamWidgetRenderer.renderWidgetBitmap(
            context = context,
            widgetSize = size,
            config = config,
            isPoweredOn = isPoweredOn,
            widthPx = widthPx,
            heightPx = heightPx
        )

        val views = RemoteViews(context.packageName, R.layout.widget_host_layout)
        views.setImageViewBitmap(R.id.widget_image, bitmap)

        // Set click intent to toggle Gleam power
        val clickIntent = Intent(context, getProviderClass(size)).apply {
            action = ACTION_WIDGET_TOGGLE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun getProviderClass(size: WidgetSize): Class<*> {
        return when (size) {
            WidgetSize.SIZE_1X1 -> GleamWidget1x1Provider::class.java
            WidgetSize.SIZE_2X2 -> GleamWidget2x2Provider::class.java
            WidgetSize.SIZE_4X4 -> GleamWidget4x4Provider::class.java
        }
    }

    /**
     * Broadcasts and redraws all 1x1, 2x2, and 4x4 active widgets across launcher.
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        val providers = listOf(
            Pair(GleamWidget1x1Provider::class.java, WidgetSize.SIZE_1X1),
            Pair(GleamWidget2x2Provider::class.java, WidgetSize.SIZE_2X2),
            Pair(GleamWidget4x4Provider::class.java, WidgetSize.SIZE_4X4)
        )

        for ((providerClass, size) in providers) {
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass))
            for (id in ids) {
                updateWidget(context, appWidgetManager, id, size)
            }
        }
    }
}
