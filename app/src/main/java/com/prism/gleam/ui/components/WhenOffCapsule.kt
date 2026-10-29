package com.prism.gleam.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme

enum class RestoreMode(val title: String, val description: String) {
    PREVIOUS(
        title = "Previous Brightness",
        description = "Brightness level before turning on auto"
    ),
    CUSTOM(
        title = "Custom Brightness",
        description = "Set a fixed brightness level"
    ),
    AUTO(
        title = "Last Auto Brightness",
        description = "Last sensor reading before turning off"
    );

    val label: String get() = title
}

@Composable
fun WhenOffCapsule(
    mode: RestoreMode,
    previewValue: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = GleamTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1.0f,
        label = "whenOffCapsuleScale"
    )

    val subtitle = mode.title

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (!enabled) 0.dp else if (colors.isDark) 6.dp else 12.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = colors.contrastButton.copy(alpha = 0.08f),
                spotColor = colors.contrastButton.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                when {
                    !enabled -> colors.cardBackground.copy(alpha = 0.6f)
                    isPressed -> colors.cardBackground.copy(alpha = 0.88f)
                    isHovered -> colors.cardBackground.copy(alpha = 0.95f)
                    else -> colors.cardBackground
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isPressed -> colors.primaryAccent.copy(alpha = 0.5f)
                    isHovered -> colors.primaryAccent.copy(alpha = 0.3f)
                    else -> colors.trackGroove
                },
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = colors.primaryAccent),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Clock / Restore Icon Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.canvas),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
                    val strokeW = 2.2.dp.toPx()
                    drawCircle(
                        color = colors.primaryAccent,
                        radius = size.width / 2f - strokeW / 2f,
                        style = Stroke(width = strokeW)
                    )
                    // Clock hands
                    val c = Offset(size.width / 2f, size.height / 2f)
                    drawLine(
                        color = colors.primaryAccent,
                        start = c,
                        end = Offset(c.x, c.y - 6.dp.toPx()),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = colors.primaryAccent,
                        start = c,
                        end = Offset(c.x + 5.dp.toPx(), c.y),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Group
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "When Screen Off",
                    color = colors.textTitle,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = subtitle,
                    color = colors.textSub,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Right Circular Button with ">" Arrow
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.contrastButton),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
                    val strokeW = 2.4.dp.toPx()
                    val arrowColor = colors.contrastButtonText
                    // Right chevron >
                    drawLine(
                        color = arrowColor,
                        start = Offset(3.dp.toPx(), 1.dp.toPx()),
                        end = Offset(10.dp.toPx(), size.height / 2f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = arrowColor,
                        start = Offset(10.dp.toPx(), size.height / 2f),
                        end = Offset(3.dp.toPx(), size.height - 1.dp.toPx()),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
