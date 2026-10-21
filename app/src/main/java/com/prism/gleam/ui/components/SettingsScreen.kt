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
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.prism.gleam.ui.theme.GleamTheme
import com.prism.gleam.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    isAmoled: Boolean,
    isAutoBrightness: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onAutoBrightnessChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    var showLicensesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Intercept hardware / gesture back to close settings smoothly
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
                        // Left-pointing sleek back chevron
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
            SectionHeader(title = "Sunlight Controller Behavior")

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
                            text = "Auto Lux Sunlight Override",
                            color = colors.textTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dynamically boost display backlight when entering direct sunlight",
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
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Section: System Permissions (Detailed generic & API names + clickable settings)
            SectionHeader(title = "System Permissions & Access")

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Access Permissions",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any item to open its system settings management page directly",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Permission 1: WRITE_SETTINGS
                    val hasWriteSettings = canWriteSystemSettings(context)
                    PermissionItemRow(
                        genericName = "Modify System Brightness",
                        apiName = "android.permission.WRITE_SETTINGS",
                        description = "Adjusts display backlight during sunlight override",
                        isGranted = hasWriteSettings,
                        actionLabel = "System Page",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            openWriteSettingsPermissionPage(context)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 2: VIBRATE
                    PermissionItemRow(
                        genericName = "Tactile Haptic Feedback",
                        apiName = "android.permission.VIBRATE",
                        description = "Simulates physical relay switch clicks for ON/OFF",
                        isGranted = true,
                        actionLabel = "App Info",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 3: REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    val isBatteryOptimized = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                    PermissionItemRow(
                        genericName = "Unrestricted Battery Execution",
                        apiName = "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
                        description = "Prevents Android from freezing sensor monitoring outdoors",
                        isGranted = isBatteryOptimized,
                        actionLabel = "Battery Page",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 4: POST_NOTIFICATIONS (API 33+)
                    val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else true

                    PermissionItemRow(
                        genericName = "Notification & Quick Tile Alerts",
                        apiName = "android.permission.POST_NOTIFICATIONS",
                        description = "Powers status bar quick toggle tile and override status",
                        isGranted = hasNotifications,
                        actionLabel = "Tile Settings",
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
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Section: Legal, Feedback & About
            SectionHeader(title = "About & Information")

            SettingsCard {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsActionRow(
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries & software notices",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            showLicensesDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = "Send Feedback & Bug Reports",
                        subtitle = "Email the development team directly",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@prism.gleam?subject=Gleam%20App%20Feedback")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {
                                showAboutDialog = true
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = "Privacy Policy",
                        subtitle = "Zero telemetry, zero trackers, 100% on-device",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            showPrivacyDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = "About Gleam",
                        subtitle = "Version 1.0.0-pro (Build 100)",
                        onClick = {
                            GleamHaptics.playSubtleClick(view)
                            showAboutDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog 1: Open Source Licenses
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = {
                Text(text = "Open Source Licenses", fontWeight = FontWeight.Bold, color = colors.textTitle)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    LicenseItem("Jetpack Compose & Material 3", "Apache License 2.0", "Google LLC")
                    LicenseItem("AndroidX Core & Lifecycle", "Apache License 2.0", "The Android Open Source Project")
                    LicenseItem("KotlinX Coroutines", "Apache License 2.0", "JetBrains s.r.o.")
                    LicenseItem("Liquid Glassmorphism UI Engine", "MIT License", "Prism Labs Open Source")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = colors.primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Dialog 2: Privacy Policy
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(text = "Privacy Policy", fontWeight = FontWeight.Bold, color = colors.textTitle)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "100% On-Device & Zero Tracking Guarantee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.primaryAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gleam is engineered with strict privacy principles:\n\n" +
                                "• No Internet Access: Gleam does not declare or request the android.permission.INTERNET permission.\n\n" +
                                "• Zero Telemetry: No analytics SDKs, trackers, or telemetry libraries are bundled.\n\n" +
                                "• Local Lux Readings: Ambient light sensor readings are processed entirely in volatile memory and are never saved or stored to disk.\n\n" +
                                "• System Write Settings: Used solely to adjust the display brightness level when sunlight override is active.",
                        fontSize = 12.5.sp,
                        color = colors.textSub,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Understood", color = colors.primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    // Dialog 3: About Gleam Page
    if (showAboutDialog) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sensorName = lightSensor?.name ?: "Hardware Lux Sensor Simulated"
        val sensorVendor = lightSensor?.vendor ?: "System Default"

        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Gleam Lux", fontWeight = FontWeight.Bold, color = colors.textTitle, fontSize = 17.sp)
                        Text(text = "Sunlight Override Utility", color = colors.textSub, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column {
                    InfoPair("Version", "1.0.0-pro (Release 100)")
                    InfoPair("Architecture", "Kotlin 2.0 / Compose 1.7")
                    InfoPair("Target SDK", "Android API 36")
                    InfoPair("Light Sensor", sensorName)
                    InfoPair("Sensor Vendor", sensorVendor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Crafted with love by Prism Labs.\nInspired by liquid glass & tactile porcelain.",
                        fontSize = 11.sp,
                        color = colors.textMuted,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = colors.primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.cardBackground
        )
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
private fun PermissionItemRow(
    genericName: String,
    apiName: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    val colors = GleamTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.canvas.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = genericName,
                    color = colors.textTitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isGranted) "Granted" else "Required",
                        color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = apiName,
                color = colors.primaryAccent,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = description,
                color = colors.textSub,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Action Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground)
                .border(1.dp, colors.trackBackground, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionLabel,
                color = colors.textTitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
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

@Composable
private fun LicenseItem(name: String, license: String, author: String) {
    val colors = GleamTheme.colors
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textTitle)
        Text(text = "$license • $author", fontSize = 11.sp, color = colors.textSub)
    }
}

@Composable
private fun InfoPair(label: String, value: String) {
    val colors = GleamTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.textSub, fontSize = 12.sp)
        Text(text = value, color = colors.textTitle, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
