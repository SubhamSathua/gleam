package com.prism.gleam.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.R
import com.prism.gleam.data.GleamPreferences
import com.prism.gleam.ui.theme.GleamTheme
import com.prism.gleam.ui.theme.UrbanistFontFamily

class WidgetSettingsActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // Determine widget size from appWidgetId provider info
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val info = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)
        } else null

        val providerName = info?.provider?.className ?: ""
        val widgetSize = when {
            providerName.contains("1x1") -> WidgetSize.SIZE_1X1
            providerName.contains("4x4") -> WidgetSize.SIZE_4X4
            else -> WidgetSize.SIZE_2X2
        }

        setContent {
            GleamTheme {
                WidgetSettingsScreen(
                    widgetId = appWidgetId,
                    widgetSize = widgetSize,
                    onSave = { config ->
                        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            WidgetPreferences.saveConfig(this, appWidgetId, config)
                            GleamWidgetManager.updateWidget(this, appWidgetManager, appWidgetId, widgetSize)
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultValue)
                        } else {
                            // If opened directly from app preview, also update all widgets
                            GleamWidgetManager.updateAllWidgets(this)
                            setResult(RESULT_OK)
                        }
                        finish()
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun WidgetSettingsScreen(
    widgetId: Int,
    widgetSize: WidgetSize,
    onSave: (WidgetConfig) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val colors = GleamTheme.colors
    val scrollState = rememberScrollState()

    val initialConfig = remember(widgetId) {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetPreferences.loadConfig(context, widgetId)
        } else {
            WidgetConfig()
        }
    }

    var selectedTheme by remember { mutableStateOf(initialConfig.theme) }
    var selectedColorStyle by remember { mutableStateOf(initialConfig.colorStyle) }
    var selectedOpacity by remember { mutableIntStateOf(initialConfig.opacityPercent) }
    var selectedRadius by remember { mutableIntStateOf(initialConfig.radiusDp) }
    var selectedSmoothing by remember { mutableIntStateOf(initialConfig.smoothingPercent) }

    val currentConfig = WidgetConfig(
        theme = selectedTheme,
        colorStyle = selectedColorStyle,
        opacityPercent = selectedOpacity,
        radiusDp = selectedRadius,
        smoothingPercent = selectedSmoothing
    )

    val isPoweredOn = remember { GleamPreferences.isPoweredOn(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header: Widget Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widget Settings",
                    color = colors.textTitle,
                    fontSize = 24.sp,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Wallpaper Preview Card with live rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // Background Lagoon Wallpaper
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_lagoon),
                    contentDescription = "Wallpaper Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Widget Live Bitmap Preview
                val density = LocalDensity.current.density
                val previewBitmap = remember(currentConfig, isPoweredOn, widgetSize) {
                    val previewWidthPx = when (widgetSize) {
                        WidgetSize.SIZE_1X1 -> (96 * density).toInt()
                        WidgetSize.SIZE_2X2 -> (200 * density).toInt()
                        WidgetSize.SIZE_4X4 -> (180 * density).toInt()
                    }
                    val previewHeightPx = when (widgetSize) {
                        WidgetSize.SIZE_1X1 -> (96 * density).toInt()
                        WidgetSize.SIZE_2X2 -> (96 * density).toInt()
                        WidgetSize.SIZE_4X4 -> (180 * density).toInt()
                    }

                    GleamWidgetRenderer.renderWidgetBitmap(
                        context = context,
                        widgetSize = widgetSize,
                        config = currentConfig,
                        isPoweredOn = isPoweredOn,
                        widthPx = previewWidthPx,
                        heightPx = previewHeightPx
                    )
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Widget Preview",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Settings Controls Card (Matching Settings.jpg)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 4.dp else 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = colors.contrastButton.copy(alpha = 0.05f),
                        spotColor = colors.contrastButton.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardBackground)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Control 1: Opacity
                    Discrete3StopSlider(
                        title = "Opacity",
                        stops = listOf(50, 70, 100),
                        stopLabels = listOf("50", "70", "100"),
                        currentValue = selectedOpacity,
                        valueLabel = "$selectedOpacity%",
                        onValueChange = { selectedOpacity = it }
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    WidgetDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Control 2: Colours (<select> / Chip selector)
                    Text(
                        text = "Colours",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetColorStyle.values().forEach { style ->
                            val isSelected = selectedColorStyle == style
                            SelectChip(
                                text = style.label,
                                isSelected = isSelected,
                                onClick = { selectedColorStyle = style },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    WidgetDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Control 3: Theme (<select> / Chip selector)
                    Text(
                        text = "Theme",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WidgetTheme.values().forEach { themeOption ->
                            val isSelected = selectedTheme == themeOption
                            SelectChip(
                                text = themeOption.label,
                                isSelected = isSelected,
                                onClick = { selectedTheme = themeOption },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    WidgetDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Control 4: Radius
                    val radiusLabel = when (selectedRadius) {
                        20 -> "Minimal (20)"
                        40 -> "Rounded (40)"
                        else -> "Capsule (70)"
                    }
                    Discrete3StopSlider(
                        title = "Radius",
                        stops = listOf(20, 40, 70),
                        stopLabels = listOf("Minimal", "Rounded", "Capsule"),
                        currentValue = selectedRadius,
                        valueLabel = radiusLabel,
                        onValueChange = { selectedRadius = it }
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    WidgetDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    // Control 5: Corner Smoothing
                    val smoothingLabel = when (selectedSmoothing) {
                        0 -> "None (0)"
                        50 -> "Minimal (50)"
                        else -> "Extreme (100)"
                    }
                    Discrete3StopSlider(
                        title = "Corner Smoothing",
                        stops = listOf(0, 50, 100),
                        stopLabels = listOf("None", "Minimal", "Extreme"),
                        currentValue = selectedSmoothing,
                        valueLabel = smoothingLabel,
                        onValueChange = { selectedSmoothing = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 4. Bottom Action Bar (Cancel / Save buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.cardSubtle)
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textTitle,
                        fontSize = 15.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.contrastButton)
                        .clickable { onSave(currentConfig) }
                        .padding(horizontal = 34.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = colors.canvas,
                        fontSize = 15.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Clean discrete 3-stop slider matching Settings.jpg design.
 */
@Composable
fun Discrete3StopSlider(
    title: String,
    stops: List<Int>,
    stopLabels: List<String>,
    currentValue: Int,
    valueLabel: String,
    onValueChange: (Int) -> Unit
) {
    val colors = GleamTheme.colors
    val selectedIndex = stops.indexOf(currentValue).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.textTitle,
                fontSize = 15.sp,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valueLabel,
                color = colors.primaryAccent,
                fontSize = 13.sp,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom Slider Track with 3 Stops and Thumb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Track Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.trackGroove)
            )

            // 3 Stop Anchors & Active Thumb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stops.forEachIndexed { index, stopValue ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onValueChange(stopValue) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            // Active glowing orange thumb (matching Settings.jpg)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF13151A))
                                    .border(3.dp, colors.primaryAccent, CircleShape)
                            )
                        } else {
                            // Inactive dot anchor
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.textSub.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        // Stop Labels below track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stopLabels.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                Text(
                    text = label,
                    color = if (isSelected) colors.textTitle else colors.textSub,
                    fontSize = 11.5.sp,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Chip / Dropdown item for <select> options.
 */
@Composable
fun SelectChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GleamTheme.colors
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) colors.contrastButton else colors.trackGroove.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.canvas else colors.textTitle,
            fontSize = 12.sp,
            fontFamily = UrbanistFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun WidgetDivider() {
    val colors = GleamTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.trackGroove.copy(alpha = 0.5f))
    )
}
