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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onAutoBrightnessChange: (Boolean) -> Unit,
    onMinBrightnessChange: (Int) -> Unit,
    onMaxBrightnessChange: (Int) -> Unit,
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
                        text = "Preferences & Diagnostics",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 2. Section: Appearance & Themes
            SectionHeader(title = "Appearance & Theme")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Interface Theme",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose between porcelain glaze, dark obsidian, or match system",
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
                                        text = "Pure AMOLED Pitch Black",
                                        color = colors.textTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Uses pure #000000 background to maximize battery savings on OLED",
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

            // 3. Section: App Brightness Behavior
            SectionHeader(title = "Display Control")

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
                                text = "Automatically boost screen in direct sunlight",
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

                    // Minimum Brightness Floor
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
                                text = "Minimum Brightness Floor",
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
                            text = "Floor brightness applied in pitch darkness / 0 lx (Default: 10%)",
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

                    // Maximum Brightness Ceiling
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
                                text = "Maximum Boost Ceiling",
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
                            text = "Peak hardware boost applied in direct sunlight / 35k+ lx (Default: 100%)",
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

            // 4. Section: Permissions Card (Opens Dedicated Page)
            SectionHeader(title = "Permissions & Privacy")

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
                                text = "System Permissions",
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
                            text = "Brightness write, haptic vibration & status tile links",
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

            // 5. Section: About, Privacy & Feedback
            SectionHeader(title = "About & Information")

            SettingsCard {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // Send Feedback (Dedicated Feedback page with description & email)
                    SettingsActionRow(
                        title = "Send Feedback & Bug Reports",
                        subtitle = "Email the development team with device diagnostics",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            onNavigateFeedback()
                        }
                    )

                    SettingsDivider()

                    // Privacy Policy Page
                    SettingsActionRow(
                        title = "Privacy Policy",
                        subtitle = "100% on-device • Zero network permissions",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            onNavigatePrivacyPolicy()
                        }
                    )

                    SettingsDivider()

                    // About Page
                    SettingsActionRow(
                        title = "About Gleam",
                        subtitle = "Version ${AppConstants.APP_VERSION} • Open Source & Architecture",
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
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "SwitchThumb"
    )

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(CircleShape)
            .background(if (checked) colors.primaryAccent else colors.trackBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(elevation = 2.dp, shape = CircleShape)
        )
    }
}
