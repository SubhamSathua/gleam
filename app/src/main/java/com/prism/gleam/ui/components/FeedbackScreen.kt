package com.prism.gleam.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.AppConstants
import com.prism.gleam.ui.theme.GleamTheme

@Composable
fun FeedbackScreen(
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
    val sensorName = lightSensor?.name ?: "Simulated/Standard HAL"
    val sensorVendor = lightSensor?.vendor ?: "Android OS"

    val diagnosticBody = """
Hi Gleam Maintainers,

[Please describe your bug, feature request, or sensor feedback here]


--------------------------------
Device: ${Build.MANUFACTURER} ${Build.MODEL}
Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
App Version: ${AppConstants.APP_VERSION} (Build ${AppConstants.APP_BUILD_NUMBER})
Sensor Hardware: $sensorName ($sensorVendor)
--------------------------------
    """.trimIndent()

    fun openEmailClient() {
        GleamHaptics.playSubtleClick(view)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${AppConstants.SUPPORT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "[Gleam Feedback] Bug Report / Feature Request")
            putExtra(Intent.EXTRA_TEXT, diagnosticBody)
        }
        try {
            context.startActivity(emailIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No email client found. Email copied to clipboard.", Toast.LENGTH_LONG).show()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Gleam Support Email", AppConstants.SUPPORT_EMAIL))
        }
    }

    fun openGitHubIssues() {
        GleamHaptics.playSubtleClick(view)
        try {
            val url = "${AppConstants.GITHUB_REPO_URL}/issues"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
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
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // Header: Back Chevron + Title
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
                        text = "Send Feedback",
                        color = colors.textTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Questions, suggestions & bug reports",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Description Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 4.dp else 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.05f),
                        spotColor = colors.contrastButton.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "We'd love to hear from you",
                        color = colors.textTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Have feedback on the sunlight boost curve, dial physics, or experiencing sensor desynchronization on your device? You have two direct options below to reach the development team.",
                        color = colors.textSub,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header: 2 Feedback Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2 Feedback Options",
                    color = colors.textSub,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(colors.trackGroove)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─────────────────────────────────────────────────────────────────
            // Option 1: Direct Email Card (Near Mail Option)
            // ─────────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 6.dp else 10.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.08f),
                        spotColor = colors.contrastButton.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val strokeW = 2.dp.toPx()
                                drawRoundRect(
                                    color = colors.primaryAccent,
                                    size = size.copy(height = size.height * 0.75f),
                                    topLeft = Offset(0f, size.height * 0.125f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(
                                    color = colors.primaryAccent,
                                    start = Offset(0f, size.height * 0.15f),
                                    end = Offset(size.width * 0.5f, size.height * 0.5f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = colors.primaryAccent,
                                    start = Offset(size.width, size.height * 0.15f),
                                    end = Offset(size.width * 0.5f, size.height * 0.5f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Option 1: Direct Email",
                                    color = colors.primaryAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            SelectionContainer {
                                Text(
                                    text = AppConstants.SUPPORT_EMAIL,
                                    color = colors.textTitle,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { openEmailClient() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Opens your email app with your phone's hardware diagnostics pre-filled to help diagnose lux curve bugs quickly.",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action: Open Email App
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.contrastButton)
                            .clickable { openEmailClient() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open Mail Client",
                            color = colors.canvas,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary: Copy Address
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, colors.trackGroove, RoundedCornerShape(14.dp))
                            .background(colors.canvas.copy(alpha = 0.5f))
                            .clickable {
                                GleamHaptics.playSubtleClick(view)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Gleam Support Email", AppConstants.SUPPORT_EMAIL))
                                Toast.makeText(context, "Email address copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Copy Email Address",
                            color = colors.textTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─────────────────────────────────────────────────────────────────
            // Option 2: GitHub Issue Tracker Card (Directly Next to Mail Option)
            // ─────────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 6.dp else 10.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.08f),
                        spotColor = colors.contrastButton.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.contrastButton.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val strokeW = 2.dp.toPx()
                                // Code branch / Git bug icon
                                drawCircle(
                                    color = colors.textTitle,
                                    radius = 3.dp.toPx(),
                                    center = Offset(size.width * 0.3f, size.height * 0.3f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawCircle(
                                    color = colors.textTitle,
                                    radius = 3.dp.toPx(),
                                    center = Offset(size.width * 0.3f, size.height * 0.7f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawCircle(
                                    color = colors.textTitle,
                                    radius = 3.dp.toPx(),
                                    center = Offset(size.width * 0.75f, size.height * 0.5f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(
                                    color = colors.textTitle,
                                    start = Offset(size.width * 0.3f, size.height * 0.38f),
                                    end = Offset(size.width * 0.3f, size.height * 0.62f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = colors.textTitle,
                                    start = Offset(size.width * 0.38f, size.height * 0.35f),
                                    end = Offset(size.width * 0.67f, size.height * 0.48f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Option 2: Open Source Tracker",
                                color = colors.primaryAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            SelectionContainer {
                                Text(
                                    text = "GitHub Issue Tracker",
                                    color = colors.textTitle,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { openGitHubIssues() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Prefer public bug tracking? Submit issues, feature requests, and join code discussions directly on GitHub.",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action: Open GitHub Issues
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.2.dp, colors.contrastButton.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .background(colors.cardSubtle)
                            .clickable { openGitHubIssues() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Open GitHub Issues",
                                color = colors.textTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            ChevronRight()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─────────────────────────────────────────────────────────────────
            // Transparency Card: Diagnostic Preview
            // ─────────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 4.dp else 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.05f),
                        spotColor = colors.contrastButton.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Diagnostic Telemetry Included",
                        color = colors.textTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "To fix device-specific light sensor issues instantly, this data is appended when choosing Option 1:",
                        color = colors.textSub,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.canvas.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "• Model: ${Build.MANUFACTURER} ${Build.MODEL}",
                                    color = colors.textSub,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "• Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                    color = colors.textSub,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "• App Build: v${AppConstants.APP_VERSION} (#${AppConstants.APP_BUILD_NUMBER})",
                                    color = colors.textSub,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "• Sensor: $sensorName",
                                    color = colors.textSub,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
