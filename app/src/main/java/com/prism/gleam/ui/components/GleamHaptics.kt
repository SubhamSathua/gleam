package com.prism.gleam.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * High-performance tactile haptic utility tuned for both premium (LRA) and budget (ERM) motors.
 *
 * - ON: Solid, punchy mechanical switch engagement (Confirm / Heavy click)
 * - OFF: Softer, damped release tick (Reject / Clock tick)
 *
 * For budget phones with ERM motors: Uses calibrated micro-impulses (26ms vs 14ms)
 * to avoid prolonged sluggish inertia and eliminate muddy buzzy rattles.
 */
object GleamHaptics {

    fun playPowerToggle(view: View, context: Context, isTurningOn: Boolean) {
        // 1. Primary: Use Android Framework View Haptic Constants
        // Google's Hardware Abstraction Layer (HAL) maps these natively to the installed motor.
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

        val performed = view.performHapticFeedback(
            constant,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )

        // 2. Fallback: Direct calibrated Vibrator pulse if framework call returned false
        if (!performed) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val effectId = if (isTurningOn) {
                            VibrationEffect.EFFECT_CLICK
                        } else {
                            VibrationEffect.EFFECT_TICK
                        }
                        vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Tuned micro-pulse for budget ERM motors:
                        // ON: 26ms at amplitude 210 -> crisp punchy snap
                        // OFF: 14ms at amplitude 120 -> soft tactile release
                        val duration = if (isTurningOn) 26L else 14L
                        val amplitude = if (isTurningOn) 210 else 120
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(if (isTurningOn) 25L else 12L)
                    }
                }
            } catch (_: Exception) {
                // Silently ignore if device has no vibrator hardware
            }
        }
    }

    /**
     * Subtle tactile tick for secondary UI interactions (cards, shortcuts, options)
     */
    fun playSubtleClick(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}
