package com.prism.gleam.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Mode (Porcelain & Solar Glaze)
val PorcelainCanvas = Color(0xFFF4F5F7)
val PorcelainCardWhite = Color(0xFFFFFFFF)
val PorcelainCardSubtle = Color(0xFFF9FAFC)
val PorcelainTrack = Color(0xFFE4E8EF)
val PorcelainTrackGroove = Color(0xFFECEFF4)

val SolarOrange = Color(0xFFFF5A22)
val SolarOrangeBright = Color(0xFFFF7532)
val SolarPeach = Color(0xFFFFA87A)
val SolarPeachLight = Color(0xFFFFD7C2)
val SolarGlow = Color(0x66FF682B)

val TextTitleLight = Color(0xFF13151A)
val TextSubLight = Color(0xFF7E8797)
val TextMutedLight = Color(0xFFA8B2C2)
val ObsidianBlack = Color(0xFF121418)

// Dark Mode (Smoky Obsidian)
val ObsidianCanvas = Color(0xFF0D0F14)
val ObsidianCard = Color(0xFF161920)
val ObsidianCardSubtle = Color(0xFF1F242E)
val ObsidianTrack = Color(0xFF232833)
val ObsidianTrackGroove = Color(0xFF1C2029)

val SolarOrangeDark = Color(0xFFFF6E35)
val SolarPeachDark = Color(0xFFFFB28A)
val SolarGlowDark = Color(0x77FF763B)

val TextTitleDark = Color(0xFFF4F6FB)
val TextSubDark = Color(0xFF8E98AA)
val TextMutedDark = Color(0xFF5A6475)
val CardHighlightDark = Color(0x1AFFFFFF)

@Immutable
data class GleamColors(
    val canvas: Color,
    val cardBackground: Color,
    val cardSubtle: Color,
    val trackBackground: Color,
    val trackGroove: Color,
    val primaryAccent: Color,
    val primaryBright: Color,
    val primaryPeach: Color,
    val primaryPeachLight: Color,
    val primaryGlow: Color,
    val textTitle: Color,
    val textSub: Color,
    val textMuted: Color,
    val contrastButton: Color,
    val contrastButtonText: Color,
    val isDark: Boolean
)

val LightGleamColors = GleamColors(
    canvas = PorcelainCanvas,
    cardBackground = PorcelainCardWhite,
    cardSubtle = PorcelainCardSubtle,
    trackBackground = PorcelainTrack,
    trackGroove = PorcelainTrackGroove,
    primaryAccent = SolarOrange,
    primaryBright = SolarOrangeBright,
    primaryPeach = SolarPeach,
    primaryPeachLight = SolarPeachLight,
    primaryGlow = SolarGlow,
    textTitle = TextTitleLight,
    textSub = TextSubLight,
    textMuted = TextMutedLight,
    contrastButton = ObsidianBlack,
    contrastButtonText = Color.White,
    isDark = false
)

val DarkGleamColors = GleamColors(
    canvas = ObsidianCanvas,
    cardBackground = ObsidianCard,
    cardSubtle = ObsidianCardSubtle,
    trackBackground = ObsidianTrack,
    trackGroove = ObsidianTrackGroove,
    primaryAccent = SolarOrangeDark,
    primaryBright = SolarOrangeDark,
    primaryPeach = SolarPeachDark,
    primaryPeachLight = SolarPeachDark,
    primaryGlow = SolarGlowDark,
    textTitle = TextTitleDark,
    textSub = TextSubDark,
    textMuted = TextMutedDark,
    contrastButton = Color.White,
    contrastButtonText = ObsidianBlack,
    isDark = true
)

val LocalGleamColors = staticCompositionLocalOf { LightGleamColors }
