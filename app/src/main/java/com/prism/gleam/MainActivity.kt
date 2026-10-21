package com.prism.gleam

import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

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

@Composable
fun GleamMainScreen(
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = GleamTheme.colors

    var isPermissionGranted by remember { mutableStateOf(canWriteSystemSettings(context)) }
    var isPoweredOn by remember { mutableStateOf(true) }
    var currentSensorFraction by remember { mutableFloatStateOf(0.68f) }
    var restoreMode by remember { mutableStateOf(RestoreMode.PREVIOUS) }
    var previousValue by remember { mutableIntStateOf(68) }
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

    // Live Environment Light Sensor Simulator
    LaunchedEffect(isPoweredOn && isPermissionGranted) {
        if (!isPoweredOn || !isPermissionGranted) return@LaunchedEffect
        var targetFraction = 0.68f
        val targets = floatArrayOf(0.35f, 0.50f, 0.68f, 0.82f, 0.96f, 0.58f)

        while (true) {
            delay(1200)
            if (isPoweredOn && isPermissionGranted) {
                val diff = targetFraction - currentSensorFraction
                currentSensorFraction += diff * 0.15f
                previousValue = (currentSensorFraction * 100).toInt()

                if (abs(diff) < 0.03f) {
                    targetFraction = targets[Random.nextInt(targets.size)]
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar & Rose-Red Permission Warning Card
            Column(modifier = Modifier.fillMaxWidth()) {
                TopBar(
                    onSettingsClick = { isSheetVisible = true }
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
                        isPoweredOn = !isPoweredOn
                        if (isPoweredOn) {
                            currentSensorFraction = when (restoreMode) {
                                RestoreMode.PREVIOUS -> previousValue / 100f
                                RestoreMode.CUSTOM -> customFloorValue / 100f
                                RestoreMode.AUTO -> 0.68f
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
                        RestoreMode.AUTO -> 68
                    },
                    enabled = isPermissionGranted,
                    onClick = {
                        if (isPermissionGranted) {
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
                            Toast.makeText(context, "Home Screen Widget shortcut activated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShortcutClick = {
                        if (isPermissionGranted) {
                            Toast.makeText(context, "1-Tap Sun Boost app shortcut ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTileClick = {
                        if (isPermissionGranted) {
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
