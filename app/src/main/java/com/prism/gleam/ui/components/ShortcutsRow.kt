package com.prism.gleam.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme

@Composable
fun ShortcutsRow(
    onWidgetClick: () -> Unit,
    onShortcutClick: () -> Unit,
    onTileClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = GleamTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Shortcuts",
            color = if (enabled) colors.textTitle else colors.textMuted,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Widget
            ShortcutCard(
                label = "Widget",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onWidgetClick
            ) {
                // 4-grid widget icon
                Canvas(modifier = Modifier.size(20.dp)) {
                    val s = 8.dp.toPx()
                    val strokeW = 2.dp.toPx()
                    val iconColor = colors.textTitle
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(width = strokeW)
                    )
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(size.width - s, 0f),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(width = strokeW)
                    )
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(0f, size.height - s),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(width = strokeW)
                    )
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(size.width - s, size.height - s),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(width = strokeW)
                    )
                }
            }

            // Card 2: Shortcut
            ShortcutCard(
                label = "Shortcut",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onShortcutClick
            ) {
                // External launch / shortcut arrow icon
                Canvas(modifier = Modifier.size(20.dp)) {
                    val strokeW = 2.dp.toPx()
                    val iconColor = colors.textTitle
                    // Box corner
                    val path = Path().apply {
                        moveTo(size.width * 0.45f, 0f)
                        lineTo(0f, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height)
                        lineTo(size.width, size.height * 0.55f)
                    }
                    drawPath(path = path, color = iconColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

                    // Arrow
                    drawLine(
                        color = iconColor,
                        start = Offset(size.width * 0.4f, size.height * 0.6f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(size.width * 0.55f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(size.width, size.height * 0.45f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Card 3: Tile
            ShortcutCard(
                label = "Tile",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = onTileClick
            ) {
                // Quick settings lightning / tile icon
                Canvas(modifier = Modifier.size(20.dp)) {
                    val strokeW = 2.dp.toPx()
                    val iconColor = colors.textTitle
                    val path = Path().apply {
                        moveTo(size.width * 0.55f, 0f)
                        lineTo(0f, size.height * 0.55f)
                        lineTo(size.width * 0.45f, size.height * 0.55f)
                        lineTo(size.width * 0.35f, size.height)
                        lineTo(size.width, size.height * 0.45f)
                        lineTo(size.width * 0.55f, size.height * 0.45f)
                        close()
                    }
                    drawPath(path = path, color = iconColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colors = GleamTheme.colors

    Box(
        modifier = modifier
            .height(106.dp)
            .shadow(
                elevation = if (!enabled) 0.dp else if (colors.isDark) 4.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = colors.contrastButton.copy(alpha = 0.06f),
                spotColor = colors.contrastButton.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) colors.cardBackground else colors.cardBackground.copy(alpha = 0.6f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Bubble
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.cardSubtle),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Pill Label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.canvas)
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = colors.textTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
