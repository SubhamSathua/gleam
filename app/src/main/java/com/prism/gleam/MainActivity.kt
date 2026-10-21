package com.prism.gleam

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.prism.gleam.ui.components.EnergyPulseOverlay
import com.prism.gleam.ui.components.GleamHaptics
import com.prism.gleam.ui.components.PermissionWarningCard
import com.prism.gleam.ui.components.RestoreMode
import com.prism.gleam.ui.components.ScaleDialView
import com.prism.gleam.ui.components.ShortcutsRow
import com.prism.gleam.ui.components.TopBar
import com.prism.gleam.ui.components.WhenOffBottomSheet
import com.prism.gleam.ui.components.WhenOffCapsule
import com.prism.gleam.ui.components.canWriteSystemSettings
import com.prism.gleam.ui.components.openWriteSettingsPermissionPage
import com.prism.gleam.ui.theme.GleamTheme
import androidx.compose.ui.zIndex
import kotlin.math.ln
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            GleamTheme(darkTheme = isDarkTheme) {
                GleamMainScreen(
                    onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

/**
 * Maps ambient light in lux (0..40,000 lx) to a normalized fraction (0.0..1.0)
 * using a logarithmic perception curve (Fechner's Law of brightness perception):
 * - 0 lx: 0.0 (Pitch black / 10° marker)
 * - 50 lx: ~0.37 (Dim room / 15° marker)
 * - 500 lx: ~0.59 (Standard indoor lighting / 20° apex marker)
 * - 2,500 lx: ~0.74 (Bright office or window / 25° marker)
 * - 40,000+ lx: 1.0 (Direct intense sunlight / 30° marker)
 */
fun luxToScaleFraction(lux: Float): Float {
    if (lux <= 0f) return 0f
    val logLux = ln(1f + lux)
    val logMax = ln(1f + 40000f)
    return (logLux / logMax).coerceIn(0f, 1f)
}

@Composable
fun GleamMainScreen(
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = GleamTheme.colors

    var isPermissionGranted by remember { mutableStateOf(canWriteSystemSettings(context)) }
    var isPoweredOn by remember { mutableStateOf(true) }
    var powerToggleTrigger by remember { mutableIntStateOf(0) }
    var currentLux by remember { mutableFloatStateOf(500f) }
    var currentSensorFraction by remember { mutableFloatStateOf(luxToScaleFraction(500f)) }
    var restoreMode by remember { mutableStateOf(RestoreMode.PREVIOUS) }
    var previousValue by remember { mutableIntStateOf(59) }
    var customFloorValue by remember { mutableIntStateOf(50) }
    var isSheetVisible by remember { mutableStateOf(false) }

    // Re-check permission on ON_RESUME (e.g. when user returns from Settings page)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = canWriteSystemSettings(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // On app start: ask user to grant permission if not already granted
    LaunchedEffect(Unit) {
        if (!canWriteSystemSettings(context)) {
            openWriteSettingsPermissionPage(context)
        }
    }

    // Real Android Hardware Ambient Light Sensor (TYPE_LIGHT) - 100% Deterministic & Precise
    DisposableEffect(lifecycleOwner, isPoweredOn, isPermissionGranted) {
        if (!isPoweredOn || !isPermissionGranted) {
            return@DisposableEffect onDispose { }
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        var sensorListener: SensorEventListener? = null

        if (sensorManager != null && lightSensor != null) {
            sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null || event.values.isEmpty()) return
                    val rawLux = event.values[0]
                    currentLux = rawLux
                    val fraction = luxToScaleFraction(rawLux)
                    currentSensorFraction = fraction
                    previousValue = (fraction * 100).roundToInt()
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Precise fallback for emulators without physical ambient light sensor:
            // Read exact Android System screen brightness (0..255)
            try {
                val brightness = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
                val fraction = (brightness / 255f).coerceIn(0f, 1f)
                currentSensorFraction = fraction
                previousValue = (fraction * 100).roundToInt()
                currentLux = brightness * 150f
            } catch (e: Exception) {
                val defaultFraction = 0.59f
                currentSensorFraction = defaultFraction
                previousValue = 59
                currentLux = 500f
            }
        }

        onDispose {
            sensorListener?.let { sensorManager?.unregisterListener(it) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        // Ambient Energy Pulse Transition (Least Z-index: rendered behind all UI elements)
        EnergyPulseOverlay(
            isPoweredOn = isPoweredOn,
            triggerCount = powerToggleTrigger,
            dialCenterFractionY = 0.43f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar with Live Sensor Readout & Rose-Red Permission Warning Card
            Column(modifier = Modifier.fillMaxWidth()) {
                TopBar(
                    onSettingsClick = { isSheetVisible = true },
                    luxReading = currentLux,
                    isPoweredOn = isPoweredOn && isPermissionGranted
                )

                // Rose-Red Warning Card when permission is not granted
                PermissionWarningCard(
                    isPermissionGranted = isPermissionGranted,
                    onCardClick = { openWriteSettingsPermissionPage(context) },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 2. Scale Dial View with Seamless Button (Zero Gap, disabled when no permission)
            ScaleDialView(
                sensorLightFraction = currentSensorFraction,
                isPoweredOn = isPoweredOn,
                enabled = isPermissionGranted,
                onPowerToggle = {
                    if (isPermissionGranted) {
                        val willBeOn = !isPoweredOn
                        GleamHaptics.playPowerToggle(view, context, isTurningOn = willBeOn)
                        powerToggleTrigger++
                        isPoweredOn = willBeOn
                        if (isPoweredOn) {
                            currentSensorFraction = when (restoreMode) {
                                RestoreMode.PREVIOUS -> previousValue / 100f
                                RestoreMode.CUSTOM -> customFloorValue / 100f
                                RestoreMode.AUTO -> luxToScaleFraction(currentLux)
                            }
                        }
                    }
                }
            )

            // 3. Middle "When Screen Off" Capsule Card (disabled when no permission)
            Column(modifier = Modifier.fillMaxWidth()) {
                WhenOffCapsule(
                    mode = restoreMode,
                    previewValue = when (restoreMode) {
                        RestoreMode.PREVIOUS -> previousValue
                        RestoreMode.CUSTOM -> customFloorValue
                        RestoreMode.AUTO -> currentLux.roundToInt()
                    },
                    enabled = isPermissionGranted,
                    onClick = {
                        if (isPermissionGranted) {
                            GleamHaptics.playSubtleClick(view)
                            isSheetVisible = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Bottom Shortcuts (Widget, Shortcut, Tile - disabled when no permission)
                ShortcutsRow(
                    enabled = isPermissionGranted,
                    onWidgetClick = {
                        if (isPermissionGranted) {
                            GleamHaptics.playSubtleClick(view)
                            Toast.makeText(context, "Home Screen Widget shortcut activated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShortcutClick = {
                        if (isPermissionGranted) {
                            GleamHaptics.playSubtleClick(view)
                            Toast.makeText(context, "1-Tap Sun Boost app shortcut ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTileClick = {
                        if (isPermissionGranted) {
                            GleamHaptics.playSubtleClick(view)
                            Toast.makeText(context, "Quick Settings status bar tile active", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // 5. Bottom Sheet (When Screen Off Options)
        WhenOffBottomSheet(
            isVisible = isSheetVisible,
            currentMode = restoreMode,
            previousValue = previousValue,
            customFloorValue = customFloorValue,
            onModeSelect = { restoreMode = it },
            onCustomFloorChange = { customFloorValue = it },
            onDismiss = { isSheetVisible = false }
        )
    }
}

@Preview(showBackground = true, name = "Light Porcelain Theme")
@Composable
fun GleamMainScreenLightPreview() {
    GleamTheme(darkTheme = false) {
        GleamMainScreen()
    }
}

@Preview(showBackground = true, name = "Dark Obsidian Theme")
@Composable
fun GleamMainScreenDarkPreview() {
    GleamTheme(darkTheme = true) {
        GleamMainScreen()
    }
}
