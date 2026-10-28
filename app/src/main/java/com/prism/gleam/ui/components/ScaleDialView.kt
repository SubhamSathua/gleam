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
import androidx.compose.ui.zIndex
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

    // Breathing Ambient Aura Animation (Pulsing along active arc when ON)
    val infiniteTransition = rememberInfiniteTransition(label = "AmbientBreatheTransition")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val startAngle = 150f
    val sweepAngle = 240f

    Box(
        modifier = modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Background Canvas (Recessed track groove, outer hairline ticks, degree labels)
        Canvas(modifier = Modifier.size(dialSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcRadius = 105.dp.toPx()

            // (A) Draw Outer Clean Hairline Ticks & Labels (0%, 25%, 50%, 75%, 100%)
            val numTicks = 17
            val labels = listOf("0%", "25%", "50%", "75%", "100%")
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

            // (B) Inactive Recessed Ambient Track Groove (Straight ends, not capsule)
            drawArc(
                color = colors.trackBackground,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2f, arcRadius * 2f),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
            )
        }

        // LAYER 2: Center Hub: Seamless 188dp Button (Zero Gap with Scale Inner Radius)
        Box(
            modifier = Modifier.size(188.dp),
            contentAlignment = Alignment.Center
        ) {
            // Seamless Central Tactile Porcelain Button
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

        // LAYER 3: Foreground Canvas (ACTIVE ARC + AMBIENT LIGHT + KNOB ABOVE BUTTON IN Z-AXIS)
        Canvas(
            modifier = Modifier
                .size(dialSize)
                .zIndex(10f)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcRadius = 105.dp.toPx()
            val activeSweep = sweepAngle * animatedFraction.coerceIn(0f, 1f)

            if (isPoweredOn && activeSweep > 0.5f) {
                // Reversed Gradient: Start is LIGHTER (Peach Light), End at Knob is DARKER (Solar Orange)
                val kRatio = (activeSweep / 360f).coerceIn(0.005f, 1.0f)
                val arcBrush = Brush.sweepGradient(
                    0.000f to colors.primaryPeachLight,
                    kRatio * 0.35f to colors.primaryPeach,
                    kRatio * 0.70f to colors.primaryBright,
                    kRatio * 1.00f to colors.primaryAccent,
                    (kRatio * 1.001f).coerceAtMost(1f) to Color.Transparent,
                    1.000f to Color.Transparent,
                    center = center
                )

                // Rotated to startAngle so 0.0f aligns exactly with start of arc (150°)
                rotate(degrees = startAngle, pivot = center) {
                    // 1. Ambient Light Plume: from start to knob only (Diffused aura, straight start)
                    drawArc(
                        brush = arcBrush,
                        startAngle = 0f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Butt),
                        alpha = 0.35f * auraAlpha
                    )

                    // 2. Secondary Soft Glow: from start to knob only
                    drawArc(
                        brush = arcBrush,
                        startAngle = 0f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Butt),
                        alpha = 0.60f * auraAlpha
                    )

                    // 3. Luminous Core Arc: from start to knob only
                    drawArc(
                        brush = arcBrush,
                        startAngle = 0f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
                    )
                }
            }

            // (D) Signature Black Dot Knob with White Halo Ring:
            // Rendered in Foreground layer with higher Z-axis so it cleanly floats OVER the ON button
            if (isPoweredOn) {
                val currentAngleDeg = startAngle + activeSweep
                val currentAngleRad = (currentAngleDeg * PI / 180f).toFloat()
                val knobCenter = Offset(
                    center.x + arcRadius * cos(currentAngleRad),
                    center.y + arcRadius * sin(currentAngleRad)
                )

                // Soft Ambient Glow Plume on knob
                drawCircle(
                    color = colors.primaryAccent.copy(alpha = 0.45f * auraAlpha),
                    radius = 22.dp.toPx(),
                    center = knobCenter
                )
                // Outer Soft White Halo Ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = 16.dp.toPx(),
                    center = knobCenter
                )
                // Crisp White Ring
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = knobCenter
                )
                // Core Black Dot (Unclipped and proud on top)
                drawCircle(
                    color = colors.contrastButton,
                    radius = 8.5.dp.toPx(),
                    center = knobCenter
                )
            }
        }
    }
}
