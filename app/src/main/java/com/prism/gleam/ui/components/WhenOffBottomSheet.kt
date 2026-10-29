package com.prism.gleam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhenOffBottomSheet(
    isVisible: Boolean,
    currentMode: RestoreMode,
    previousValue: Int,
    customFloorValue: Int,
    onModeSelect: (RestoreMode) -> Unit,
    onCustomFloorChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val colors = GleamTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(colors.trackBackground)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "When Screen Off",
                    color = colors.textTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.canvas)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = colors.textSub,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Previous Value
            OptionItem(
                title = "Previous Brightness",
                description = "Restore level from before turning on Auto",
                isSelected = currentMode == RestoreMode.PREVIOUS,
                onClick = { onModeSelect(RestoreMode.PREVIOUS) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Custom Floor Value
            OptionItem(
                title = "Custom Brightness",
                description = "Set a specific fixed brightness level",
                isSelected = currentMode == RestoreMode.CUSTOM,
                onClick = { onModeSelect(RestoreMode.CUSTOM) }
            )

            AnimatedVisibility(visible = currentMode == RestoreMode.CUSTOM) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fixed Level",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$customFloorValue%",
                            color = colors.primaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = customFloorValue.toFloat(),
                        onValueChange = { onCustomFloorChange(it.toInt()) },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.trackBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Auto Sensor
            OptionItem(
                title = "Last Auto Brightness",
                description = "Keep the last sensor reading before turning off",
                isSelected = currentMode == RestoreMode.AUTO,
                onClick = { onModeSelect(RestoreMode.AUTO) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.contrastButton,
                    contentColor = colors.contrastButtonText
                )
            ) {
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = GleamTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        label = "optionItemScale"
    )

    val backgroundColor = when {
        isPressed -> colors.primaryAccent.copy(alpha = if (isSelected) 0.20f else 0.10f)
        isHovered -> colors.primaryAccent.copy(alpha = if (isSelected) 0.16f else 0.06f)
        isSelected -> colors.primaryAccent.copy(alpha = 0.08f)
        else -> colors.canvas
    }

    val borderColor = when {
        isSelected -> colors.primaryAccent.copy(alpha = if (isPressed) 0.9f else 0.65f)
        isHovered -> colors.textMuted.copy(alpha = 0.4f)
        else -> colors.trackBackground.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.primaryAccent),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio circle: clearly visible whether checked or unchecked
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.background(colors.primaryAccent)
                    } else {
                        Modifier
                            .background(colors.cardBackground)
                            .border(2.dp, colors.textMuted.copy(alpha = 0.55f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Description (No % near title)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textTitle,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = colors.textSub,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
