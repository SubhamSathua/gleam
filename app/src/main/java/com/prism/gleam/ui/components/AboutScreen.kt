package com.prism.gleam.ui.components

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.AppConstants
import com.prism.gleam.ui.theme.GleamTheme

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    BackHandler(onBack = onBack)

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    val sensorName = lightSensor?.name ?: "Hardware Lux Sensor Simulated"
    val sensorVendor = lightSensor?.vendor ?: "Android HAL Default"
    val sensorMaxRange = lightSensor?.maximumRange?.let { "${it.toInt()} lx" } ?: "40,000 lx"
    val sensorPower = lightSensor?.power?.let { "$it mA" } ?: "0.75 mA"

    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(width = 160, height = 160).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

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
            // Header with Back Chevron
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
                        text = "About Gleam",
                        color = colors.textTitle,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "App info & open source license",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Hero Brand Card (Porcelain & Solar Glaze - matching reference image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 6.dp else 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.06f),
                        spotColor = colors.contrastButton.copy(alpha = 0.10f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.cardBackground)
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Real App Icon with Solar Ambient Halo
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = colors.primaryAccent.copy(alpha = 0.25f),
                                spotColor = colors.primaryAccent.copy(alpha = 0.40f)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(19.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(colors.primaryAccent, colors.primaryPeach)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = AppConstants.APP_NAME,
                        color = colors.textTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Smart Sunlight Brightness Booster",
                        color = colors.textSub,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Version Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.canvas)
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "v${AppConstants.APP_VERSION} • Build ${AppConstants.APP_BUILD_NUMBER}",
                            color = colors.primaryAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Light Sensor Info Card
            SectionTitle(title = "Light Sensor Info")

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
                    DiagnosticRow(label = "Sensor Name", value = sensorName)
                    DiagnosticDivider()
                    DiagnosticRow(label = "Manufacturer", value = sensorVendor)
                    DiagnosticDivider()
                    DiagnosticRow(label = "Max Range", value = sensorMaxRange)
                    DiagnosticDivider()
                    DiagnosticRow(label = "Power Usage", value = sensorPower)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Open Source License Section
            SectionTitle(title = "Open Source License")

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Apache License 2.0",
                            color = colors.textTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Free & Open Source",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Gleam is 100% free software. You can use it, inspect the code, modify it, or share it freely with anyone.",
                        color = colors.textSub,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simple License Points
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.canvas)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "What this means for you:",
                            color = colors.textTitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LicensePoint(icon = "✓", text = "Free for everyone to use and share", isPositive = true)
                        LicensePoint(icon = "✓", text = "Source code is completely open to inspect", isPositive = true)
                        LicensePoint(icon = "✓", text = "You can modify and build your own version", isPositive = true)
                        LicensePoint(icon = "ℹ", text = "Provided as-is without warranty", isPositive = false)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clickable GitHub Repo Link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.canvas)
                            .border(1.dp, colors.trackBackground, RoundedCornerShape(14.dp))
                            .clickable {
                                GleamHaptics.playSubtleClick(view)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.GITHUB_REPO_URL))
                                context.startActivity(intent)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Source on GitHub ↗",
                            color = colors.primaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "LIBRARIES USED",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Jetpack Compose & Material 3 (UI)\n" +
                               "• AndroidX Core & Lifecycle (System)\n" +
                               "• Kotlin Coroutines (Background tasks)",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Subcomponents for About Page
// -------------------------------------------------------------------------------------------------

@Composable
private fun SectionTitle(title: String) {
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
private fun DiagnosticRow(label: String, value: String) {
    val colors = GleamTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSub,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = colors.textTitle,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DiagnosticDivider() {
    val colors = GleamTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.trackBackground.copy(alpha = 0.5f))
    )
}

@Composable
private fun LicensePoint(icon: String, text: String, isPositive: Boolean) {
    val colors = GleamTheme.colors
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            color = if (isPositive) Color(0xFF10B981) else colors.textSub,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            color = colors.textSub,
            fontSize = 12.sp
        )
    }
}
