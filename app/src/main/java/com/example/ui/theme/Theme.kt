package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ThemeOption

// Deep Cosmic Dark Theme Colors
private val CosmicPrimary = Color(0xFF00FFCC) // Vibrant Teal Cyber-mint
private val CosmicSecondary = Color(0xFF64B5F6) // Soft Neon Blue
private val CosmicTertiary = Color(0xFFFF5252) // Vibrant Tomato Coral
private val CosmicBackground = Color(0xFF0F111A) // Deep Charcoal Navy
private val CosmicSurface = Color(0xFF181C2E) // Navy Card Surface
private val CosmicOnBackground = Color(0xFFE2E6F2)
private val CosmicOnSurface = Color(0xFFFFFFFF)

private val CosmicColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    onPrimary = Color(0xFF00382C),
    secondary = CosmicSecondary,
    onSecondary = Color(0xFF002D54),
    tertiary = CosmicTertiary,
    onTertiary = Color(0xFF410002),
    background = CosmicBackground,
    surface = CosmicSurface,
    onBackground = CosmicOnBackground,
    onSurface = CosmicOnSurface
)

// Sage Mint Light Organic Colors
private val SagePrimary = Color(0xFF1E523A) // Deep Organic Sage
private val SageSecondary = Color(0xFF4A755D) // Soft Olive-green
private val SageTertiary = Color(0xFF8B5A2B) // Warm Clay
private val SageBackground = Color(0xFFF3F6F3) // Fresh Off-White
private val SageSurface = Color(0xFFFFFFFF) // White Papers
private val SageOnBackground = Color(0xFF1A211D)
private val SageOnSurface = Color(0xFF1C221F)

private val SageColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    secondary = SageSecondary,
    onSecondary = Color.White,
    tertiary = SageTertiary,
    onTertiary = Color.White,
    background = SageBackground,
    surface = SageSurface,
    onBackground = SageOnBackground,
    onSurface = SageOnSurface
)

// Lilac Blossom Playful Soft Colors
private val LilacPrimary = Color(0xFF7C3AED) // Royal Violet
private val LilacSecondary = Color(0xFF8B5CF6) // Medium Lavender
private val LilacTertiary = Color(0xFFD946EF) // Playful Fuchsia
private val LilacBackground = Color(0xFFFAF7FF) // Lavender Off-White
private val LilacSurface = Color(0xFFFFFFFF)
private val LilacOnBackground = Color(0xFF1E1533)
private val LilacOnSurface = Color(0xFF1F1634)

private val LilacColorScheme = lightColorScheme(
    primary = LilacPrimary,
    onPrimary = Color.White,
    secondary = LilacSecondary,
    onSecondary = Color.White,
    tertiary = LilacTertiary,
    onTertiary = Color.White,
    background = LilacBackground,
    surface = LilacSurface,
    onBackground = LilacOnBackground,
    onSurface = LilacOnSurface
)

// Coral Sunset Rich Charcoal Colors
private val CoralPrimary = Color(0xFFFF5722) // Energetic Warm Tangerine
private val CoralSecondary = Color(0xFFFF8A65) // Light Orange-coral
private val CoralTertiary = Color(0xFFEC407A) // Hot Pink Cherry
private val CoralBackground = Color(0xFF151010) // Warm Cocoa-black
private val CoralSurface = Color(0xFF241C1B) // Brick-graphite Card
private val CoralOnBackground = Color(0xFFFCF4F1)
private val CoralOnSurface = Color(0xFFFCFAF9)

private val CoralColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color(0xFF4E1600),
    secondary = CoralSecondary,
    onSecondary = Color(0xFF5D1D00),
    tertiary = CoralTertiary,
    onTertiary = Color(0xFF330012),
    background = CoralBackground,
    surface = CoralSurface,
    onBackground = CoralOnBackground,
    onSurface = CoralOnSurface
)

@Composable
fun MyApplicationTheme(
    selectedTheme: ThemeOption = ThemeOption.COSMIC_DEEP,
    content: @Composable () -> Unit
) {
    val colorScheme = when (selectedTheme) {
        ThemeOption.COSMIC_DEEP -> CosmicColorScheme
        ThemeOption.SAGE_MINT -> SageColorScheme
        ThemeOption.LILAC_BLOSSOM -> LilacColorScheme
        ThemeOption.CORAL_SUNSET -> CoralColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
