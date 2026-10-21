package com.prism.gleam.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme

fun canWriteSystemSettings(context: Context): Boolean {
    return Settings.System.canWrite(context)
}

fun openWriteSettingsPermissionPage(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}

@Composable
fun PermissionWarningCard(
    isPermissionGranted: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GleamTheme.colors

    // Rose-Red Colors matching the theme
    val roseBg = if (colors.isDark) Color(0xFF281016) else Color(0xFFFFF0F3)
    val roseBorder = if (colors.isDark) Color(0xFF4A1E27) else Color(0xFFFFD4DC)
    val roseText = if (colors.isDark) Color(0xFFFF6680) else Color(0xFFE02E49)
    val roseSub = if (colors.isDark) Color(0xFFD48A96) else Color(0xFF9E4353)

    AnimatedVisibility(
        visible = !isPermissionGranted,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (colors.isDark) 4.dp else 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = roseText.copy(alpha = 0.15f),
                    spotColor = roseText.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(roseBg)
                .border(width = 1.dp, color = roseBorder, shape = RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCardClick
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rose-red alert circle icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(roseText.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val strokeW = 2.2.dp.toPx()
                        // Outer alert circle
                        drawCircle(
                            color = roseText,
                            radius = size.width / 2f - strokeW / 2f,
                            style = Stroke(width = strokeW)
                        )
                        // Exclamation line & dot
                        drawLine(
                            color = roseText,
                            start = Offset(size.width / 2f, 5.dp.toPx()),
                            end = Offset(size.width / 2f, 11.dp.toPx()),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = roseText,
                            radius = 1.3.dp.toPx(),
                            center = Offset(size.width / 2f, 15.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change System Settings",
                        color = roseText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "Permission needed to adjust brightness. Tap to grant.",
                        color = roseSub,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Action pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(roseText)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Grant",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
