package com.prism.gleam.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.prism.gleam.data.GleamPreferences

/**
 * High-performance tactile haptic utility tuned for both premium (LRA) and budget (ERM) motors.
 *
 * - ON: Solid, punchy mechanical switch engagement (Confirm / Heavy click)
 * - OFF: Softer, damped release tick (Reject / Clock tick)
 *
 * Respects user's Vibration Setting and ensures guaranteed physical haptic response
 * across all Android OEMs (including Samsung One UI).
 */
object GleamHaptics {

    fun playPowerToggle(view: View, context: Context, isTurningOn: Boolean) {
        if (!GleamPreferences.isHapticFeedback(context)) return

        // 1. Primary: Use Android Framework View Haptic Constants
        val constant = if (isTurningOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
                HapticFeedbackConstants.TOGGLE_ON
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.CONTEXT_CLICK
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
                HapticFeedbackConstants.TOGGLE_OFF
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30
                HapticFeedbackConstants.REJECT
            } else {
                HapticFeedbackConstants.CLOCK_TICK
            }
        }

        view.performHapticFeedback(
            constant,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )

        // 2. Direct Hardware Vibrator invocation:
        // Many Android devices (especially Samsung One UI) silently swallow view haptics
        // if system-wide "touch vibration" is toggled off in device settings.
        // Direct vibrator invocation guarantees physical sensation.
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val duration = if (isTurningOn) 36L else 18L
                val amplitude = if (isTurningOn) 220 else 150

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(duration, amplitude)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attrs = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
                            .build()
                        vibrator.vibrate(effect, attrs)
                    } else {
                        vibrator.vibrate(effect)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (_: Exception) {
            // Silently ignore if device has no vibrator hardware
        }
    }

    /**
     * Subtle tactile tick for secondary UI interactions (cards, shortcuts, options)
     */
    fun playSubtleClick(view: View, context: Context? = null) {
        if (context != null && !GleamPreferences.isHapticFeedback(context)) return
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}
