package com.prism.gleam.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.prism.gleam.AppConstants
import com.prism.gleam.ui.theme.GleamTheme
import com.prism.gleam.ui.theme.ThemeMode
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    isAmoled: Boolean,
    isAutoBrightness: Boolean,
    minBrightness: Int,
    maxBrightness: Int,
    isHapticFeedback: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onAutoBrightnessChange: (Boolean) -> Unit,
    onMinBrightnessChange: (Int) -> Unit,
    onMaxBrightnessChange: (Int) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onNavigatePermissions: () -> Unit,
    onNavigateFeedback: () -> Unit,
    onNavigatePrivacyPolicy: () -> Unit,
    onNavigateAbout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    BackHandler(onBack = onBack)

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
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // 1. Header (Back Chevron + Title)
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
                        text = "Settings",
                        color = colors.textTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "App preferences & controls",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 2. Section: Appearance
            SectionHeader(title = "Appearance")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose Light, Dark, or follow System",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // 3-way Theme Selector Segmented Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.canvas)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colors.cardBackground else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            GleamHaptics.playSubtleClick(view)
                                            onThemeModeChange(mode)
                                        }
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (mode) {
                                        ThemeMode.SYSTEM -> "System"
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                    },
                                    color = if (isSelected) colors.primaryAccent else colors.textSub,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Pure AMOLED Black Toggle (shown if Dark or System Dark)
                    val isEligibleForAmoled = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && colors.isDark)
                    AnimatedVisibility(visible = isEligibleForAmoled) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "True Black (AMOLED)",
                                        color = colors.textTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Saves battery on OLED screens with pure black",
                                        color = colors.textSub,
                                        fontSize = 11.5.sp
                                    )
                                }
                                GleamSwitch(
                                    checked = isAmoled,
                                    onCheckedChange = {
                                        GleamHaptics.playSubtleClick(view)
                                        onAmoledChange(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Section: Brightness
            SectionHeader(title = "Brightness")

            SettingsCard {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Brightness",
                                color = colors.textTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatically boost screen brightness in sunlight",
                                color = colors.textSub,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        GleamSwitch(
                            checked = isAutoBrightness,
                            onCheckedChange = {
                                GleamHaptics.playSubtleClick(view)
                                onAutoBrightnessChange(it)
                            }
                        )
                    }

                    SettingsDivider()

                    // Minimum Brightness
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Minimum Brightness",
                                color = colors.textTitle,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$minBrightness%",
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Lowest screen brightness in the dark (Default: 10%)",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp, bottom = 4.dp)
                        )

                        Slider(
                            value = minBrightness.toFloat(),
                            onValueChange = {
                                val newMin = it.roundToInt().coerceIn(0, (maxBrightness - 5).coerceAtLeast(0))
                                onMinBrightnessChange(newMin)
                            },
                            valueRange = 0f..50f,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primaryAccent,
                                activeTrackColor = colors.primaryAccent,
                                inactiveTrackColor = colors.trackGroove
                            )
                        )
                    }

                    SettingsDivider()

                    // Maximum Brightness
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Maximum Brightness",
                                color = colors.textTitle,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$maxBrightness%",
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Highest screen boost in bright sunlight (Default: 100%)",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp, bottom = 4.dp)
                        )

                        Slider(
                            value = maxBrightness.toFloat(),
                            onValueChange = {
                                val newMax = it.roundToInt().coerceIn((minBrightness + 5).coerceAtMost(100), 100)
                                onMaxBrightnessChange(newMax)
                            },
                            valueRange = 50f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primaryAccent,
                                activeTrackColor = colors.primaryAccent,
                                inactiveTrackColor = colors.trackGroove
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Section: Vibration
            SectionHeader(title = "Vibration")

            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vibration Feedback",
                            color = colors.textTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vibrate when tapping buttons and toggles",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    GleamSwitch(
                        checked = isHapticFeedback,
                        onCheckedChange = {
                            onHapticFeedbackChange(it)
                            if (it) {
                                GleamHaptics.playPowerToggle(view, context, isTurningOn = true)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Section: Permissions
            SectionHeader(title = "Permissions")

            val hasWriteSettings = canWriteSystemSettings(context)
            val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
            val totalPerms = 3
            val grantedCount = listOf(hasWriteSettings, true, hasNotifications).count { it }

            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            GleamHaptics.playSubtleClick(view)
                            onNavigatePermissions()
                        }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "App Permissions",
                                color = colors.textTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (grantedCount == totalPerms) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else colors.primaryAccent.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$grantedCount/$totalPerms Active",
                                    color = if (grantedCount == totalPerms) Color(0xFF10B981) else colors.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Allow Gleam to adjust screen brightness",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    ChevronRight()
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Section: More Info
            SectionHeader(title = "More Info")

            SettingsCard {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // Send Feedback
                    SettingsActionRow(
                        title = "Send Feedback",
                        subtitle = "Report an issue or suggest a feature",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            onNavigateFeedback()
                        }
                    )

                    SettingsDivider()

                    // Privacy Policy Page
                    SettingsActionRow(
                        title = "Privacy Policy",
                        subtitle = "No data collected • 100% private & offline",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            onNavigatePrivacyPolicy()
                        }
                    )

                    SettingsDivider()

                    // About Page
                    SettingsActionRow(
                        title = "About Gleam",
                        subtitle = "Version ${AppConstants.APP_VERSION} • Open source license & info",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            onNavigateAbout()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Subcomponents for Settings
// -------------------------------------------------------------------------------------------------

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = GleamTheme.colors.primaryAccent,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
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
    ) {
        content()
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = GleamTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = colors.textSub,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        ChevronRight()
    }
}

@Composable
fun ChevronRight() {
    val colors = GleamTheme.colors
    Canvas(modifier = Modifier.size(14.dp)) {
        val strokeW = 2.dp.toPx()
        drawLine(
            color = colors.textMuted,
            start = Offset(size.width * 0.3f, size.height * 0.15f),
            end = Offset(size.width * 0.7f, size.height * 0.50f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colors.textMuted,
            start = Offset(size.width * 0.7f, size.height * 0.50f),
            end = Offset(size.width * 0.3f, size.height * 0.85f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SettingsDivider() {
    val colors = GleamTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(colors.trackBackground.copy(alpha = 0.5f))
    )
}

@Composable
private fun GleamSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = GleamTheme.colors
    val animProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SwitchProgress"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.primaryAccent else colors.trackBackground,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SwitchTrackColor"
    )

    Canvas(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
    ) {
        val corner = CornerRadius(size.height / 2f, size.height / 2f)

        // 1. Capsule Track (smoothly transitions between trackBackground and primaryAccent)
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = corner
        )

        // 2. Mathematically centered circular thumb (symmetric 3dp margins on all 4 sides)
        val thumbRadius = 11.dp.toPx()
        val margin = 3.dp.toPx()
        val minX = margin + thumbRadius
        val maxX = size.width - margin - thumbRadius
        val currentThumbX = minX + (maxX - minX) * animProgress
        val thumbCenter = Offset(currentThumbX, size.height / 2f)

        // Soft natural drop shadow under thumb (eliminates Skia RenderNode elevation polygon artifacts)
        drawCircle(
            color = Color.Black.copy(alpha = 0.16f),
            radius = thumbRadius + 0.6.dp.toPx(),
            center = Offset(thumbCenter.x, thumbCenter.y + 0.8.dp.toPx())
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.08f),
            radius = thumbRadius + 1.2.dp.toPx(),
            center = Offset(thumbCenter.x, thumbCenter.y + 1.2.dp.toPx())
        )

        // Pure smooth white circular knob
        drawCircle(
            color = Color.White,
            radius = thumbRadius,
            center = thumbCenter
        )
    }
}
