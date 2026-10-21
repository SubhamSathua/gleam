package com.prism.gleam.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.prism.gleam.ui.theme.GleamTheme

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    BackHandler(onBack = onBack)

    val hasWriteSettings = canWriteSystemSettings(context)
    val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    val totalCount = 3
    val grantedCount = listOf(hasWriteSettings, true, hasNotifications).count { it }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = if (colors.isDark) 4.dp else 8.dp,
                            shape = CircleShape,
                            ambientColor = colors.contrastButton.copy(alpha = 0.08f),
                            spotColor = colors.contrastButton.copy(alpha = 0.12f)
                        )
                        .clip(CircleShape)
                        .background(colors.cardBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                GleamHaptics.playSubtleClick(view)
                                onBack()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val strokeW = 2.4.dp.toPx()
                        drawLine(
                            color = colors.textTitle,
                            start = Offset(size.width * 0.65f, size.height * 0.15f),
                            end = Offset(size.width * 0.25f, size.height * 0.50f),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = colors.textTitle,
                            start = Offset(size.width * 0.25f, size.height * 0.50f),
                            end = Offset(size.width * 0.65f, size.height * 0.85f),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "System Permissions",
                        color = colors.textTitle,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Diagnostic Status & Settings Links",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Distinct System Diagnostic Health Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                colors.primaryAccent.copy(alpha = if (colors.isDark) 0.18f else 0.10f),
                                colors.primaryPeach.copy(alpha = if (colors.isDark) 0.08f else 0.04f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        colors.primaryAccent.copy(alpha = if (colors.isDark) 0.35f else 0.20f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (grantedCount == totalCount) Color(0xFF10B981) else colors.primaryAccent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val strokeW = 2.4.dp.toPx()
                            if (grantedCount == totalCount) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width * 0.22f, size.height * 0.52f),
                                    end = Offset(size.width * 0.45f, size.height * 0.75f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width * 0.45f, size.height * 0.75f),
                                    end = Offset(size.width * 0.82f, size.height * 0.30f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            } else {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width / 2f, size.height * 0.25f),
                                    end = Offset(size.width / 2f, size.height * 0.60f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 1.6.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height * 0.78f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (grantedCount == totalCount) "All Capabilities Ready" else "Configuration Required",
                            color = colors.textTitle,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$grantedCount of $totalCount permissions active",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Permission Card 1: WRITE_SETTINGS
            PermissionDetailCard(
                title = "Modify System Brightness",
                apiConstant = "android.permission.WRITE_SETTINGS",
                purpose = "Allows Gleam to adjust the hardware display backlight when the ambient sunlight sensor detects intense outdoor lighting.",
                isGranted = hasWriteSettings,
                buttonLabel = "Open System Settings Page",
                onClick = {
                    GleamHaptics.playSubtleClick(view)
                    openWriteSettingsPermissionPage(context)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permission Card 2: VIBRATE
            PermissionDetailCard(
                title = "Tactile Haptic Vibration",
                apiConstant = "android.permission.VIBRATE",
                purpose = "Drives precision tactile micro-clicks on power toggle and controls, calibrated for both flagship and budget vibration motors.",
                isGranted = true,
                buttonLabel = "Open App Info Settings",
                onClick = {
                    GleamHaptics.playSubtleClick(view)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permission Card 3: POST_NOTIFICATIONS
            PermissionDetailCard(
                title = "Status Bar Quick Tile Alerts",
                apiConstant = "android.permission.POST_NOTIFICATIONS",
                purpose = "Allows adding Gleam to your Android quick settings pull-down shade for 1-tap sunlight boost without opening the app.",
                isGranted = hasNotifications,
                buttonLabel = "Open Notification Settings",
                onClick = {
                    GleamHaptics.playSubtleClick(view)
                    try {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Mobile-optimized card layout: stacked cleanly so labels, badges, and code don't cause awkward wraps
 */
@Composable
private fun PermissionDetailCard(
    title: String,
    apiConstant: String,
    purpose: String,
    isGranted: Boolean,
    buttonLabel: String,
    onClick: () -> Unit
) {
    val colors = GleamTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 4.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = colors.contrastButton.copy(alpha = 0.05f),
                spotColor = colors.contrastButton.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.cardBackground)
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title on Left, Status Pill on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.textTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isGranted) "Granted" else "Action Required",
                        color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code Chip for API Name
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.canvas)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = apiConstant,
                    color = colors.primaryAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = purpose,
                color = colors.textSub,
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Full-Width Mobile-Optimized Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.canvas)
                    .border(1.dp, colors.trackBackground, RoundedCornerShape(14.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buttonLabel,
                        color = colors.textTitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "→",
                        color = colors.primaryAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
