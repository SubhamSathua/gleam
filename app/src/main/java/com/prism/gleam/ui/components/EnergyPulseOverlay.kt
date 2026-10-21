package com.prism.gleam.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.prism.gleam.ui.theme.GleamTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Energy particle representation for the expanding & contracting pulse
 */
private data class EnergyParticle(
    val baseAngle: Float,
    val distanceRatio: Float,
    val radiusDp: Float,
    val speedOffset: Float,
    val colorIndex: Int,
    val tangentialSpeed: Float
)

@Composable
fun EnergyPulseOverlay(
    isPoweredOn: Boolean,
    triggerCount: Int,
    modifier: Modifier = Modifier,
    dialCenterFractionY: Float = 0.42f // approximate vertical center of dial
) {
    val colors = GleamTheme.colors
    val progressAnim = remember { Animatable(0f) }

    // Seeded deterministic particles (cached, zero runtime garbage collection)
    val particles = remember {
        val rand = Random(1337)
        List(52) { i ->
            val angle = (i.toFloat() / 52f) * 2f * PI.toFloat() + (rand.nextFloat() - 0.5f) * 0.35f
            val dist = 0.45f + rand.nextFloat() * 0.55f
            val r = 2.0f + rand.nextFloat() * 3.5f
            val speedOff = 0.85f + rand.nextFloat() * 0.3f
            val col = rand.nextInt(4)
            val tang = (rand.nextFloat() - 0.5f) * 0.45f
            EnergyParticle(angle, dist, r, speedOff, col, tang)
        }
    }

    // Seeded micro-grain noise dots along shockwave front
    val grainOffsets = remember {
        val rand = Random(404)
        List(120) {
            val angle = rand.nextFloat() * 2f * PI.toFloat()
            val radiusJitter = (rand.nextFloat() - 0.5f) * 0.18f
            val alphaJitter = 0.08f + rand.nextFloat() * 0.16f
            val sizeJitter = 1.0f + rand.nextFloat() * 1.8f
            Triple(angle, radiusJitter, Pair(alphaJitter, sizeJitter))
        }
    }

    // Trigger 1-second spread-out and return animation whenever power state toggles
    LaunchedEffect(triggerCount) {
        if (triggerCount > 0) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
    }

    val progress = progressAnim.value
    if (progress <= 0f || progress >= 1f) return

    // Spread out (0.0 -> 0.5) and Come back (0.5 -> 1.0) via sine wave curve
    // sin(progress * PI) peaks at 1.0 at 0.5s, then returns cleanly to 0.0 at 1.0s
    val expansion = sin(progress * PI.toFloat())

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * dialCenterFractionY)
        val maxScreenRadius = sqrt(w * w + h * h) * 0.75f

        val currentWaveRadius = maxScreenRadius * expansion

        // 1. Full-Screen Gradient Energy Plume (Atmospheric Bloom)
        val plumeAlpha = (0.28f * expansion).coerceIn(0f, 0.45f)
        val baseColor = if (isPoweredOn) colors.primaryAccent else colors.textMuted
        val secondaryColor = if (isPoweredOn) colors.primaryPeach else colors.trackBackground

        val radialGradient = Brush.radialGradient(
            colors = listOf(
                baseColor.copy(alpha = plumeAlpha * 1.1f),
                secondaryColor.copy(alpha = plumeAlpha * 0.6f),
                colors.primaryPeachLight.copy(alpha = plumeAlpha * 0.25f),
                Color.Transparent
            ),
            center = center,
            radius = (currentWaveRadius * 1.25f).coerceAtLeast(10f)
        )
        drawCircle(
            brush = radialGradient,
            center = center,
            radius = currentWaveRadius * 1.25f
        )

        // 2. Multi-Layer Radiant Bloom Shockwave Rings (Expanding then Retracting)
        val ringColor = if (isPoweredOn) colors.primaryAccent else colors.textSub
        val ringAlpha = (0.55f * expansion).coerceIn(0f, 0.8f)

        // Outer Soft Bloom Ring
        drawCircle(
            color = ringColor.copy(alpha = ringAlpha * 0.25f),
            center = center,
            radius = currentWaveRadius,
            style = Stroke(width = 36.dp.toPx())
        )

        // Mid Bloom Ring
        drawCircle(
            color = secondaryColor.copy(alpha = ringAlpha * 0.55f),
            center = center,
            radius = currentWaveRadius,
            style = Stroke(width = 18.dp.toPx())
        )

        // Crisp Energy Wavefront
        drawCircle(
            color = Color.White.copy(alpha = ringAlpha * 0.85f),
            center = center,
            radius = currentWaveRadius,
            style = Stroke(width = 3.dp.toPx())
        )

        // 3. Tactile Grain Texture (Micro-particles vibrating along the energy wave)
        grainOffsets.forEach { (angle, radJitter, meta) ->
            val (dotAlpha, dotSize) = meta
            val effectiveRadius = currentWaveRadius * (1f + radJitter)
            if (effectiveRadius > 5f) {
                val gx = center.x + effectiveRadius * cos(angle)
                val gy = center.y + effectiveRadius * sin(angle)

                drawCircle(
                    color = if (isPoweredOn) {
                        colors.primaryPeachLight.copy(alpha = (dotAlpha * expansion).coerceIn(0f, 1f))
                    } else {
                        Color.White.copy(alpha = (dotAlpha * expansion * 0.7f).coerceIn(0f, 1f))
                    },
                    radius = dotSize.dp.toPx(),
                    center = Offset(gx, gy)
                )
            }
        }

        // 4. Energy Particles (Embers spreading out radially then swirling back in)
        particles.forEach { p ->
            val particleDist = (maxScreenRadius * p.distanceRatio * expansion * p.speedOffset)
            val currentAngle = p.baseAngle + (progress * p.tangentialSpeed * 2f * PI.toFloat())
            val px = center.x + particleDist * cos(currentAngle)
            val py = center.y + particleDist * sin(currentAngle)

            val pAlpha = (expansion * (0.4f + p.distanceRatio * 0.6f)).coerceIn(0f, 0.95f)
            val particleColor = when (p.colorIndex) {
                0 -> Color.White.copy(alpha = pAlpha)
                1 -> colors.primaryAccent.copy(alpha = pAlpha)
                2 -> colors.primaryPeach.copy(alpha = pAlpha)
                else -> colors.primaryBright.copy(alpha = pAlpha)
            }

            // Particle Ambient Halo Bloom
            drawCircle(
                color = particleColor.copy(alpha = pAlpha * 0.35f),
                radius = (p.radiusDp * 2.4f).dp.toPx(),
                center = Offset(px, py)
            )

            // Core Energy Sparkle
            drawCircle(
                color = particleColor,
                radius = p.radiusDp.dp.toPx(),
                center = Offset(px, py)
            )
        }

        // 5. Central Hub Absorption Glaze (Core energy returning to center button)
        val coreRadius = 94.dp.toPx() * (1f + 0.35f * expansion)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = ringAlpha * 0.6f),
                    secondaryColor.copy(alpha = ringAlpha * 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius
            ),
            center = center,
            radius = coreRadius
        )
    }
}
