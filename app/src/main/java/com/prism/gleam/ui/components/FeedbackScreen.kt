package com.prism.gleam.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.prism.gleam.AppConstants
import com.prism.gleam.ui.theme.GleamTheme

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    var showWebView by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isWebViewLoading by remember { mutableStateOf(true) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    val sensorName = lightSensor?.name ?: "Simulated/Standard HAL"
    val sensorVendor = lightSensor?.vendor ?: "Android OS"

    val diagnosticSummary = """
Device: ${Build.MANUFACTURER} ${Build.MODEL}
Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
App Version: v${AppConstants.APP_VERSION} (Build ${AppConstants.APP_BUILD_NUMBER})
Sensor: $sensorName ($sensorVendor)
    """.trimIndent()

    fun openBrowser(url: String) {
        GleamHaptics.playSubtleClick(view)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    if (showWebView) {
        BackHandler {
            if (webViewInstance?.canGoBack() == true) {
                webViewInstance?.goBack()
            } else {
                showWebView = false
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.canvas)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                                    showWebView = false
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

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Feedback Form",
                            color = colors.textTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "tally.so/r/aOO1oZ",
                            color = colors.textSub,
                            fontSize = 11.5.sp
                        )
                    }

                    // External open button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardBackground)
                            .clickable { openBrowser(AppConstants.FEEDBACK_FORM_URL) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Browser ↗",
                            color = colors.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Loading bar
                AnimatedVisibility(
                    visible = isWebViewLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.primaryAccent,
                        trackColor = colors.trackGroove
                    )
                }

                // In-App WebView
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(v: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(v, url, favicon)
                                    isWebViewLoading = true
                                }

                                override fun onPageFinished(v: WebView?, url: String?) {
                                    super.onPageFinished(v, url)
                                    isWebViewLoading = false
                                }
                            }
                            loadUrl(AppConstants.FEEDBACK_FORM_URL)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
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
                            text = "Feedback form & GitHub community",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ─────────────────────────────────────────────────────────────
                // Option 1: In-App Feedback Form (Tally)
                // ─────────────────────────────────────────────────────────────
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
                                    // Form / Document checklist glyph
                                    drawRoundRect(
                                        color = colors.primaryAccent,
                                        size = size.copy(width = size.width * 0.8f, height = size.height * 0.95f),
                                        topLeft = Offset(size.width * 0.1f, 0f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                    )
                                    drawLine(
                                        color = colors.primaryAccent,
                                        start = Offset(size.width * 0.25f, size.height * 0.3f),
                                        end = Offset(size.width * 0.70f, size.height * 0.3f),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = colors.primaryAccent,
                                        start = Offset(size.width * 0.25f, size.height * 0.55f),
                                        end = Offset(size.width * 0.70f, size.height * 0.55f),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Option 1: Feedback Form",
                                    color = colors.primaryAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Online Feedback Form",
                                    color = colors.textTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Submit bugs, suggestions, and feedback directly through our quick interactive form. Opens instantly inside the app.",
                            color = colors.textSub,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action: Open In-App Feedback Form
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.contrastButton)
                                .clickable {
                                    GleamHaptics.playSubtleClick(view)
                                    showWebView = true
                                }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Open Feedback Form",
                                color = colors.canvas,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ─────────────────────────────────────────────────────────────
                // Option 2: GitHub Tracker (Issues, Discussions, Security)
                // ─────────────────────────────────────────────────────────────
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
                                    text = "Option 2: Open Source",
                                    color = colors.primaryAccent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "GitHub Community",
                                    color = colors.textTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // GitHub Sub-action 1: Issues
                        GitHubActionRow(
                            title = "GitHub Issues",
                            subtitle = "Report bugs, crashes, or suggest features",
                            onClick = { openBrowser(AppConstants.GITHUB_ISSUES_URL) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // GitHub Sub-action 2: Discussions
                        GitHubActionRow(
                            title = "GitHub Discussions",
                            subtitle = "Share ideas, ask questions & talk with devs",
                            onClick = { openBrowser(AppConstants.GITHUB_DISCUSSIONS_URL) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // GitHub Sub-action 3: Private Security Advisories
                        GitHubActionRow(
                            title = "Security Advisories",
                            subtitle = "Report private security vulnerabilities",
                            onClick = { openBrowser(AppConstants.GITHUB_SECURITY_URL) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ─────────────────────────────────────────────────────────────
                // Diagnostic Telemetry Card with One-Tap Copy
                // ─────────────────────────────────────────────────────────────
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Device Diagnostics",
                                color = colors.textTitle,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.12f))
                                    .clickable {
                                        GleamHaptics.playSubtleClick(view)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Gleam Device Diagnostics", diagnosticSummary))
                                        Toast.makeText(context, "Diagnostics copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Copy",
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Hardware information you can include in your feedback to help resolve sensor issues:",
                            color = colors.textSub,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
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
                                        text = "• Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                        color = colors.textSub,
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "• App: v${AppConstants.APP_VERSION} (#${AppConstants.APP_BUILD_NUMBER})",
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
}

@Composable
private fun GitHubActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = GleamTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.trackGroove, RoundedCornerShape(14.dp))
            .background(colors.canvas.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textTitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = colors.textSub,
                    fontSize = 11.5.sp
                )
            }
            Text(
                text = "↗",
                color = colors.textSub,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
