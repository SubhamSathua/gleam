package com.prism.gleam.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    luxReading: Float? = null,
    isPoweredOn: Boolean = true
) {
    val colors = GleamTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Gleam Lux",
                color = colors.textTitle,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp
            )
            Text(
                text = if (isPoweredOn && luxReading != null) {
                    "Live Sensor • ${luxReading.toInt()} lx"
                } else if (!isPoweredOn) {
                    "Adaptive Light Controller • Standby"
                } else {
                    "Adaptive Light Controller"
                },
                color = if (isPoweredOn && luxReading != null) colors.primaryAccent else colors.textSub,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Settings Icon Button (⚙)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = if (colors.isDark) 4.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = colors.contrastButton.copy(alpha = 0.06f),
                    spotColor = colors.contrastButton.copy(alpha = 0.1f)
                )
                .clip(CircleShape)
                .background(colors.cardBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSettingsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val strokeW = 2.dp.toPx()
                val gearColor = colors.textTitle
                val c = Offset(size.width / 2f, size.height / 2f)

                // Central circle
                drawCircle(
                    color = gearColor,
                    radius = 4.dp.toPx(),
                    center = c,
                    style = Stroke(width = strokeW)
                )

                // 6 gear cogs
                val outerR = 9.dp.toPx()
                val innerR = 6.dp.toPx()
                for (i in 0 until 6) {
                    val angle = (i * 60f * PI / 180f).toFloat()
                    val p1 = Offset(c.x + innerR * cos(angle), c.y + innerR * sin(angle))
                    val p2 = Offset(c.x + outerR * cos(angle), c.y + outerR * sin(angle))
                    drawLine(
                        color = gearColor,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
