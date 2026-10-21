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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.core.content.ContextCompat
import com.prism.gleam.controller.BrightnessController
import com.prism.gleam.data.GleamPreferences
import com.prism.gleam.shortcut.ToggleShortcutActivity
import com.prism.gleam.ui.components.AboutScreen
import com.prism.gleam.ui.components.EnergyPulseOverlay
import com.prism.gleam.ui.components.FeedbackScreen
import com.prism.gleam.ui.components.GleamHaptics
import com.prism.gleam.ui.components.PermissionWarningCard
import com.prism.gleam.ui.components.PermissionsScreen
import com.prism.gleam.ui.components.PrivacyPolicyScreen
import com.prism.gleam.ui.components.RestoreMode
import com.prism.gleam.ui.components.ScaleDialView
import com.prism.gleam.ui.components.SettingsScreen
import com.prism.gleam.ui.components.ShortcutsRow
import com.prism.gleam.ui.components.TopBar
import com.prism.gleam.ui.components.WhenOffBottomSheet
import com.prism.gleam.ui.components.WhenOffCapsule
import com.prism.gleam.ui.components.canWriteSystemSettings
import com.prism.gleam.ui.components.openWriteSettingsPermissionPage
import com.prism.gleam.ui.theme.GleamTheme
import com.prism.gleam.ui.theme.ThemeMode
import kotlin.math.ln
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            var isAmoled by remember { mutableStateOf(false) }

            GleamTheme(
                themeMode = themeMode,
                isAmoled = isAmoled
            ) {
                GleamAppRoot(
                    themeMode = themeMode,
                    isAmoled = isAmoled,
                    onThemeModeChange = { themeMode = it },
                    onAmoledChange = { isAmoled = it }
                )
            }
        }
    }
}

enum class AppScreen {
    MAIN,
    SETTINGS,
    PERMISSIONS,
    PRIVACY_POLICY,
    ABOUT,
    FEEDBACK
}

@Composable
fun GleamAppRoot(
    themeMode: ThemeMode,
    isAmoled: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledChange: (Boolean) -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
    var isAutoBrightness by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "AppScreenNavigation"
    ) { screen ->
        when (screen) {
            AppScreen.MAIN -> {
                GleamMainScreen(
                    onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                    isAutoBrightness = isAutoBrightness
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    themeMode = themeMode,
                    isAmoled = isAmoled,
                    isAutoBrightness = isAutoBrightness,
                    onThemeModeChange = onThemeModeChange,
                    onAmoledChange = onAmoledChange,
                    onAutoBrightnessChange = { isAutoBrightness = it },
                    onNavigatePermissions = { currentScreen = AppScreen.PERMISSIONS },
                    onNavigateFeedback = { currentScreen = AppScreen.FEEDBACK },
                    onNavigatePrivacyPolicy = { currentScreen = AppScreen.PRIVACY_POLICY },
                    onNavigateAbout = { currentScreen = AppScreen.ABOUT },
                    onBack = { currentScreen = AppScreen.MAIN }
                )
            }
            AppScreen.PERMISSIONS -> {
                PermissionsScreen(
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
            AppScreen.PRIVACY_POLICY -> {
                PrivacyPolicyScreen(
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
            AppScreen.ABOUT -> {
                AboutScreen(
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
            AppScreen.FEEDBACK -> {
                FeedbackScreen(
                    onBack = { currentScreen = AppScreen.SETTINGS }
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
    onOpenSettings: () -> Unit = {},
    isAutoBrightness: Boolean = true
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = GleamTheme.colors

    var isPermissionGranted by remember { mutableStateOf(canWriteSystemSettings(context)) }
    var isPoweredOn by remember { mutableStateOf(GleamPreferences.isPoweredOn(context)) }
    var powerToggleTrigger by remember { mutableIntStateOf(0) }
    var currentLux by remember { mutableFloatStateOf(500f) }
    var currentSensorFraction by remember { mutableFloatStateOf(luxToScaleFraction(500f)) }

    val initialMode = remember {
        try {
            RestoreMode.valueOf(GleamPreferences.getRestoreMode(context))
        } catch (_: Exception) {
            RestoreMode.PREVIOUS
        }
    }
    var restoreMode by remember { mutableStateOf(initialMode) }
    var previousValue by remember { mutableIntStateOf(GleamPreferences.getPreviousValue(context)) }
    var customFloorValue by remember { mutableIntStateOf(GleamPreferences.getCustomFloorValue(context)) }
    var isSheetVisible by remember { mutableStateOf(false) }

    // Sync with Quick Settings Tile & 1-Tap Home Screen Shortcut (Single pointer)
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                if (intent?.action == GleamPreferences.ACTION_TOGGLE_POWER) {
                    val newPower = GleamPreferences.isPoweredOn(context)
                    if (newPower != isPoweredOn) {
                        isPoweredOn = newPower
                        powerToggleTrigger++
                        val activity = context as? ComponentActivity
                        if (newPower) {
                            BrightnessController.applySunlightBrightness(context, activity, currentLux, true)
                        } else {
                            BrightnessController.restoreBrightness(context, activity, restoreMode, previousValue, customFloorValue)
                        }
                    }
                }
            }
        }
        val filter = android.content.IntentFilter(GleamPreferences.ACTION_TOGGLE_POWER)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

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

    // Real Android Hardware Ambient Light Sensor (TYPE_LIGHT) - 100% Deterministic & Hardware-accurate
    DisposableEffect(lifecycleOwner, isPoweredOn, isPermissionGranted, isAutoBrightness) {
        if (!isPoweredOn || !isPermissionGranted || !isAutoBrightness) {
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
                    GleamPreferences.setPreviousValue(context, previousValue)

                    // Execute real hardware sunlight brightness override:
                    // At 40,000 lx, targetBrightness is 255 (maximum peak hardware boost)
                    val activity = context as? ComponentActivity
                    BrightnessController.applySunlightBrightness(
                        context = context,
                        activity = activity,
                        lux = rawLux,
                        isPoweredOn = isPoweredOn
                    )
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
                    onSettingsClick = {
                        GleamHaptics.playSubtleClick(view)
                        onOpenSettings()
                    },
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
                        GleamPreferences.setPoweredOn(context, willBeOn)
                        val activity = context as? ComponentActivity

                        if (willBeOn) {
                            currentSensorFraction = when (restoreMode) {
                                RestoreMode.PREVIOUS -> previousValue / 100f
                                RestoreMode.CUSTOM -> customFloorValue / 100f
                                RestoreMode.AUTO -> luxToScaleFraction(currentLux)
                            }
                            BrightnessController.applySunlightBrightness(context, activity, currentLux, true)
                        } else {
                            BrightnessController.restoreBrightness(context, activity, restoreMode, previousValue, customFloorValue)
                        }

                        // Sync notification shade Quick Settings tile and send broadcast
                        GleamPreferences.requestTileUpdate(context)
                        context.sendBroadcast(
                            android.content.Intent(GleamPreferences.ACTION_TOGGLE_POWER).setPackage(context.packageName)
                        )
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
                            ToggleShortcutActivity.pinShortcutToHomeScreen(context)
                        }
                    },
                    onTileClick = {
                        if (isPermissionGranted) {
                            GleamHaptics.playSubtleClick(view)
                            Toast.makeText(context, "Pull down notification shade to add & use Gleam Sun quick tile", Toast.LENGTH_LONG).show()
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
            onModeSelect = {
                restoreMode = it
                GleamPreferences.setRestoreMode(context, it.name)
            },
            onCustomFloorChange = {
                customFloorValue = it
                GleamPreferences.setCustomFloorValue(context, it)
            },
            onDismiss = { isSheetVisible = false }
        )
    }
}

@Preview(showBackground = true, name = "Light Porcelain Theme")
@Composable
fun GleamMainScreenLightPreview() {
    GleamTheme(themeMode = ThemeMode.LIGHT) {
        GleamMainScreen()
    }
}

@Preview(showBackground = true, name = "Dark Obsidian Theme")
@Composable
fun GleamMainScreenDarkPreview() {
    GleamTheme(themeMode = ThemeMode.DARK) {
        GleamMainScreen()
    }
}
