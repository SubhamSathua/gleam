package com.prism.gleam.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prism.gleam.ui.theme.GleamTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScaleDialView(
    sensorLightFraction: Float, // 0f to 1f
    isPoweredOn: Boolean,
    onPowerToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dialSize: Dp = 320.dp
) {
    val colors = GleamTheme.colors

    // Smooth, instant spring tracking for physical sensor readings (zero lag)
    val animatedFraction by animateFloatAsState(
        targetValue = if (isPoweredOn && enabled) sensorLightFraction.coerceIn(0f, 1f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SensorFractionAnimation"
    )

    // Breathing Ambient Aura Animation (Pulsing when ON)
    val infiniteTransition = rememberInfiniteTransition(label = "AmbientBreatheTransition")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        // 1. Canvas: Renders outer ticks, degree labels, and soft multi-layered luminous arc
        Canvas(modifier = Modifier.size(dialSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcRadius = 105.dp.toPx()
            val startAngle = 150f
            val sweepAngle = 240f

            // (A) Draw Outer Clean Hairline Ticks & Labels (10°, 15°, 20°, 25°, 30°)
            val numTicks = 17
            val labels = listOf("10°", "15°", "20°", "25°", "30°")
            val tickPaint = Paint().apply {
                color = colors.textMuted.toArgb()
                textSize = 13.5.dp.toPx()
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }

            for (i in 0 until numTicks) {
                val fraction = i / (numTicks - 1f)
                val angleDeg = startAngle + fraction * sweepAngle
                val angleRad = (angleDeg * PI / 180f).toFloat()

                val isMajor = (i % 4 == 0)
                val r1 = 124.dp.toPx()
                val r2 = if (isMajor) 134.dp.toPx() else 130.dp.toPx()

                val x1 = center.x + r1 * cos(angleRad)
                val y1 = center.y + r1 * sin(angleRad)
                val x2 = center.x + r2 * cos(angleRad)
                val y2 = center.y + r2 * sin(angleRad)

                drawLine(
                    color = if (isMajor) colors.textSub.copy(alpha = 0.6f) else colors.textMuted.copy(alpha = 0.4f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                if (isMajor) {
                    val labelIndex = i / 4
                    if (labelIndex in labels.indices) {
                        val textR = 150.dp.toPx()
                        val tx = center.x + textR * cos(angleRad)
                        val ty = center.y + textR * sin(angleRad)
                        val fontMetrics = tickPaint.fontMetrics
                        val yCenterOffset = (fontMetrics.ascent + fontMetrics.descent) / 2f
                        drawContext.canvas.nativeCanvas.drawText(
                            labels[labelIndex],
                            tx,
                            ty - yCenterOffset,
                            tickPaint
                        )
                    }
                }
            }

            // (B) Layer 1: Recessed Ambient Track Groove
            drawArc(
                color = colors.trackBackground,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2f, arcRadius * 2f),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )

            // (C) Layer 2 & 3: Luminous Frosted Gradient Arc with Precise Rotation
            val activeSweep = sweepAngle * animatedFraction.coerceIn(0f, 1f)

            if (isPoweredOn && activeSweep > 0.5f) {
                // Arc spans 240 deg of 360 deg (ratio = 240/360 = 2/3)
                // Color stops are proportionally mapped so 0.0f aligns with startAngle (150°)
                val arcBrush = Brush.sweepGradient(
                    0.00f to colors.primaryAccent,
                    0.18f to colors.primaryBright,
                    0.35f to colors.primaryPeach,
                    0.52f to colors.primaryPeachLight,
                    0.67f to Color.White.copy(alpha = 0.35f),
                    1.00f to colors.primaryAccent,
                    center = center
                )

                // Rotated to startAngle so 0.0f is exactly at 150 degrees
                rotate(degrees = startAngle, pivot = center) {
                    // Layer 2: Soft ambient diffusion plume
                    drawArc(
                        brush = arcBrush,
                        startAngle = 0f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 32.dp.toPx(), cap = StrokeCap.Round),
                        alpha = 0.45f
                    )

                    // Layer 3: Soft Luminous Core Arc
                    drawArc(
                        brush = arcBrush,
                        startAngle = 0f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // (D) Signature Black Dot Knob with White Halo Ring from Reference Image (Always precise)
            if (isPoweredOn) {
                val currentAngleDeg = startAngle + activeSweep
                val currentAngleRad = (currentAngleDeg * PI / 180f).toFloat()
                val knobCenter = Offset(
                    center.x + arcRadius * cos(currentAngleRad),
                    center.y + arcRadius * sin(currentAngleRad)
                )

                // Soft Ambient Glow Plume
                drawCircle(
                    color = colors.primaryAccent.copy(alpha = 0.35f),
                    radius = 18.dp.toPx(),
                    center = knobCenter
                )
                // Outer Soft White Halo
                drawCircle(
                    color = Color.White.copy(alpha = 0.70f),
                    radius = 15.dp.toPx(),
                    center = knobCenter
                )
                // Crisp White Ring
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = knobCenter
                )
                // Core Black Dot
                drawCircle(
                    color = colors.contrastButton,
                    radius = 8.5.dp.toPx(),
                    center = knobCenter
                )
            }
        }

        // 2. Center Hub: Seamless 188dp Button (Zero Gap with Scale Inner Diameter)
        Box(
            modifier = Modifier.size(188.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Breathing Light Aura directly hugging the perimeter
            if (isPoweredOn && enabled) {
                Box(
                    modifier = Modifier
                        .size(208.dp)
                        .scale(auraScale)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.primaryAccent.copy(alpha = 0.5f * auraAlpha),
                                        colors.primaryPeach.copy(alpha = 0.2f * auraAlpha),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width / 2f
                                )
                            )
                        }
                )
            }

            // Seamless Central Tactile Porcelain Button (fills inner scale with zero gap)
            Box(
                modifier = Modifier
                    .size(188.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isPoweredOn && enabled) {
                                listOf(colors.cardBackground, colors.cardSubtle)
                            } else {
                                listOf(colors.cardSubtle, colors.trackBackground)
                            }
                        )
                    )
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPowerToggle
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Central Power Toggle Glyph
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = if (isPoweredOn && enabled) {
                                Brush.linearGradient(
                                    listOf(colors.primaryAccent, colors.primaryPeach)
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(colors.trackBackground, colors.trackGroove)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val strokeW = 2.8.dp.toPx()
                        val glyphColor = if (isPoweredOn && enabled) Color.White else colors.textMuted
                        val c = Offset(size.width / 2f, size.height / 2f)

                        // Top vertical power line
                        drawLine(
                            color = glyphColor,
                            start = Offset(c.x, 2.dp.toPx()),
                            end = Offset(c.x, size.height * 0.45f),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )

                        // 300-deg open power circle
                        drawArc(
                            color = glyphColor,
                            startAngle = -60f,
                            sweepAngle = 300f,
                            useCenter = false,
                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
