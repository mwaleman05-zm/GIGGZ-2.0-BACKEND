package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Mode: Default White version
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF059669), // Emerald Green
    secondary = Color(0xFF10B981), // Mint Green
    tertiary = GiggzGold, // Gold
    background = Color(0xFFF9FAFB), // White / soft grey
    surface = Color(0xFFFFFFFF), // White cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827), // Dark grey / black text
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6), // Soft grey borders / panels
    onSurfaceVariant = Color(0xFF4B5563)
)

// Dark Mode: Dark Luxury Premium version
private val DarkColorScheme = darkColorScheme(
    primary = GiggzPremiumMintPrimary, // Mint Green Accent #2ED3A3
    secondary = GiggzPremiumMintHover, // Hover / Soft variant #45E6B8
    tertiary = GiggzPremiumMintPressed, // Pressed / Darker variant #1BAA82
    background = GiggzPremiumDarkBg, // Primary Background #121417
    surface = GiggzPremiumDarkCardDefault, // Default Cards #1A1D22
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = GiggzPremiumTextPrimary, // Primary text #FFFFFF
    onSurface = GiggzPremiumTextPrimary, // Primary text #FFFFFF
    surfaceVariant = GiggzPremiumDarkCardElevated, // Elevated / Featured Cards #20252C
    onSurfaceVariant = GiggzPremiumTextSecondary, // Secondary text #A7B0B8
    tertiaryContainer = GiggzPremiumDarkInnerOverlay, // Inner soft overlay layer #0F1318
    onTertiaryContainer = GiggzPremiumTextMuted // Muted text #6C7682
)

@Composable
fun MyApplicationTheme(
    themeName: String = "Light Mode",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Treat "Dark Mode" or "Forest Mint" as dark theme
    val isDark = themeName == "Dark Mode" || themeName == "Forest Mint"
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
