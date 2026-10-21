package com.prism.gleam.controller

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.prism.gleam.luxToScaleFraction
import com.prism.gleam.ui.components.RestoreMode
import com.prism.gleam.ui.components.canWriteSystemSettings
import kotlin.math.roundToInt

object BrightnessController {
    private const val TAG = "GleamBrightness"

    private var originalBrightnessMode: Int? = null
    private var originalBrightnessVal: Int? = null

    /**
     * Maps ambient light in lux (0..40,000+ lx) to Android system brightness (0..255):
     * - Under 10 lx: 15 (gentle dim floor)
     * - ~500 lx (indoor room): ~155 (comfortable indoor level)
     * - >= 35,000 lx (direct intense sunlight): 255 (MAXIMUM PEAK HARDWARE BACKLIGHT!)
     */
    fun luxToBrightness(lux: Float): Int {
        if (lux <= 5f) return 15
        if (lux >= 35000f) return 255

        val fraction = luxToScaleFraction(lux)
        return (15 + fraction * (255 - 15)).roundToInt().coerceIn(15, 255)
    }

    /**
     * Executes real hardware sunlight brightness override.
     */
    fun applySunlightBrightness(
        context: Context,
        activity: Activity?,
        lux: Float,
        isPoweredOn: Boolean
    ) {
        if (!isPoweredOn) return

        if (!canWriteSystemSettings(context)) {
            Log.w(TAG, "Cannot write brightness: WRITE_SETTINGS permission not granted")
            return
        }

        try {
            val resolver = context.contentResolver

            // Cache original system settings on first override
            if (originalBrightnessMode == null) {
                originalBrightnessMode = Settings.System.getInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                )
            }
            if (originalBrightnessVal == null) {
                originalBrightnessVal = Settings.System.getInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )
            }

            // If system auto-brightness is fighting us, switch to manual mode so our sunlight override rules
            val currentMode = Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            if (currentMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            }

            // Compute target brightness: at 40k lux, this is exactly 255 (max peak boost)
            val targetBrightness = luxToBrightness(lux)
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS,
                targetBrightness
            )

            // Also update the current WindowManager layout attributes for instant zero-latency response
            activity?.let { act ->
                act.runOnUiThread {
                    val lp = act.window.attributes
                    lp.screenBrightness = targetBrightness / 255f
                    act.window.attributes = lp
                }
            }

            Log.d(TAG, "Applied sunlight brightness: $targetBrightness/255 at $lux lx")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing system brightness", e)
        }
    }

    /**
     * Restores display brightness when Gleam is turned OFF or screen turns off.
     */
    fun restoreBrightness(
        context: Context,
        activity: Activity?,
        mode: RestoreMode,
        previousValue: Int,
        customFloorValue: Int
    ) {
        if (!canWriteSystemSettings(context)) return

        try {
            val resolver = context.contentResolver

            when (mode) {
                RestoreMode.PREVIOUS -> {
                    val restoreVal = (previousValue * 2.55f).roundToInt().coerceIn(15, 255)
                    Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, restoreVal)
                }
                RestoreMode.CUSTOM -> {
                    val floorVal = (customFloorValue * 2.55f).roundToInt().coerceIn(15, 255)
                    Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, floorVal)
                }
                RestoreMode.AUTO -> {
                    // Restore Android's default adaptive automatic brightness mode
                    Settings.System.putInt(
                        resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    )
                }
            }

            // Reset WindowManager local attribute to system default
            activity?.let { act ->
                act.runOnUiThread {
                    val lp = act.window.attributes
                    lp.screenBrightness = -1.0f // resets to system brightness
                    act.window.attributes = lp
                }
            }

            Log.d(TAG, "Restored brightness for mode: $mode")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring brightness", e)
        }
    }

    /**
     * Triggers a one-shot sensor sample to immediately boost brightness when activated
     * from external entry points such as Quick Settings Tile or 1-tap Home Screen Shortcut.
     */
    fun triggerOneShotSunlightBoost(context: Context) {
        if (!canWriteSystemSettings(context)) return

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (sensorManager != null && lightSensor != null) {
            val handler = Handler(Looper.getMainLooper())
            var completed = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (completed) return
                    completed = true
                    sensorManager.unregisterListener(this)
                    val lux = event?.values?.firstOrNull() ?: 40000f
                    applySunlightBrightness(context, null, lux, true)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)

            // Safety timeout: apply boost after 200ms if sensor is slow to report
            handler.postDelayed({
                if (!completed) {
                    completed = true
                    sensorManager.unregisterListener(listener)
                    applySunlightBrightness(context, null, 40000f, true)
                }
            }, 200)
        } else {
            // Fallback for devices without hardware light sensor
            applySunlightBrightness(context, null, 40000f, true)
        }
    }
}
