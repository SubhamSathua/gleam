package com.prism.gleam.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.prism.gleam.ui.theme.GleamTheme
import kotlin.math.sqrt

/**
 * Premium ambient background energy transition (rendered behind all UI elements).
 *
 * - ON Animation: Slow, luxurious spreading outward wave using light tile / peach-glaze colors,
 *   gradually fading away across the entire canvas over 1.0s.
 * - OFF Animation: Primary solar orange gradient fades in from the screen edges,
 *   pulls smoothly inward toward the central hub, and fades out cleanly into the power button over 1.0s.
 */
@Composable
fun EnergyPulseOverlay(
    isPoweredOn: Boolean,
    triggerCount: Int,
    modifier: Modifier = Modifier,
    dialCenterFractionY: Float = 0.43f
) {
    val colors = GleamTheme.colors
    val progressAnim = remember { Animatable(1f) }

    LaunchedEffect(triggerCount) {
        if (triggerCount > 0) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    val progress = progressAnim.value
    if (progress >= 1f || triggerCount == 0) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * dialCenterFractionY)
        val maxScreenRadius = sqrt(w * w + h * h) * 0.95f
        val minDialRadius = 94f * density // matches central button radius in px

        if (isPoweredOn) {
            // =========================================================================
            // ON ANIMATION: Spread Out Gradient & Fade Gradually (Color = Light Tile)
            // =========================================================================
            // Luxurious slow spread easing
            val spreadEasing = CubicBezierEasing(0.14f, 0.70f, 0.22f, 1.0f)
            val spreadFactor = spreadEasing.transform(progress)

            // Radius slowly spreads from button rim to fill whole background
            val currentRadius = minDialRadius + (maxScreenRadius - minDialRadius) * spreadFactor

            // Alpha starts soft and fades out gradually over the entire 1 second
            val fadeOutAlpha = (1f - progress).coerceIn(0f, 1f)
            val peakIntensity = if (colors.isDark) 0.55f else 0.42f
            val alpha = peakIntensity * fadeOutAlpha

            // Color: "Light tile" / warm soft porcelain peach glaze
            val lightTileColor = colors.primaryPeachLight
            val midPeachColor = colors.primaryPeach
            val outerAmbient = colors.cardSubtle

            // Broad, ultra-diffuse atmospheric bloom
            val atmosphericBrush = Brush.radialGradient(
                0.00f to lightTileColor.copy(alpha = alpha * 0.95f),
                0.35f to lightTileColor.copy(alpha = alpha * 0.65f),
                0.65f to midPeachColor.copy(alpha = alpha * 0.30f),
                0.90f to outerAmbient.copy(alpha = alpha * 0.10f),
                1.00f to Color.Transparent,
                center = center,
                radius = currentRadius.coerceAtLeast(10f)
            )

            drawCircle(
                brush = atmosphericBrush,
                center = center,
                radius = currentRadius
            )

            // Secondary soft core wash for tactile depth
            val innerCoreRadius = (currentRadius * 0.45f).coerceAtLeast(minDialRadius)
            val coreBrush = Brush.radialGradient(
                0.00f to Color.White.copy(alpha = alpha * 0.50f),
                0.60f to lightTileColor.copy(alpha = alpha * 0.25f),
                1.00f to Color.Transparent,
                center = center,
                radius = innerCoreRadius
            )

            drawCircle(
                brush = coreBrush,
                center = center,
                radius = innerCoreRadius
            )

        } else {
            // =========================================================================
            // OFF ANIMATION: Fade In, Pull In Gradient, & Fade Out (Color = Primary Color)
            // =========================================================================
            // Pulling inward curve (gravity pull toward center)
            val pullEasing = CubicBezierEasing(0.40f, 0.00f, 0.20f, 1.0f)
            val pullProgress = pullEasing.transform(progress)

            // Radius shrinks from outer screen edge down into the central button
            val currentRadius = maxScreenRadius - (maxScreenRadius - minDialRadius) * pullProgress

            // Envelope: Fades in during 0.0 -> 0.25, remains steady, fades out cleanly during 0.60 -> 1.0
            val fadeIn = (progress / 0.25f).coerceIn(0f, 1f)
            val fadeOut = ((1f - progress) / 0.40f).coerceIn(0f, 1f)
            val peakIntensity = if (colors.isDark) 0.65f else 0.48f
            val alpha = peakIntensity * fadeIn * fadeOut

            // Color: "Primary color" (Rich solar orange, bright ember, saturated core)
            val primaryColor = colors.primaryAccent
            val brightColor = colors.primaryBright
            val warmAmber = colors.primaryPeach

            val pullInBrush = Brush.radialGradient(
                0.00f to brightColor.copy(alpha = alpha * 0.90f),
                0.40f to primaryColor.copy(alpha = alpha * 0.70f),
                0.75f to warmAmber.copy(alpha = alpha * 0.35f),
                0.95f to primaryColor.copy(alpha = alpha * 0.10f),
                1.00f to Color.Transparent,
                center = center,
                radius = currentRadius.coerceAtLeast(10f)
            )

            drawCircle(
                brush = pullInBrush,
                center = center,
                radius = currentRadius
            )

            // Converging core glow condensing into the center glyph
            val coreRadius = (minDialRadius * 1.5f * (1f - progress * 0.5f)).coerceAtLeast(10f)
            val coreBrush = Brush.radialGradient(
                0.00f to primaryColor.copy(alpha = alpha * 0.85f),
                0.70f to brightColor.copy(alpha = alpha * 0.40f),
                1.00f to Color.Transparent,
                center = center,
                radius = coreRadius
            )

            drawCircle(
                brush = coreBrush,
                center = center,
                radius = coreRadius
            )
        }
    }
}
