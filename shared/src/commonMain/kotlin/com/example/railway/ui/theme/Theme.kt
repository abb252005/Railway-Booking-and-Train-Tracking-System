package com.example.railway.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RailwayPrimaryDark,
    onPrimary = RailwayOnPrimaryDark,
    primaryContainer = RailwayPrimaryContainerDark,
    onPrimaryContainer = Color.White,
    secondary = RailwaySecondaryDark,
    onSecondary = RailwayOnSecondaryDark,
    secondaryContainer = MapBlueDark,
    onSecondaryContainer = RailwayBackgroundDark,
    tertiary = RailwayTertiaryDark,
    onTertiary = RailwayOnTertiaryDark,
    tertiaryContainer = Color(0xFF4D0014),
    onTertiaryContainer = Color.White,
    background = RailwayBackgroundDark,
    onBackground = Color.White,
    surface = RailwaySurfaceDark,
    onSurface = RailwayOnSurfaceDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFEBEBF5).copy(alpha = 0.6f),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF4D0001),
    onErrorContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RailwayPrimaryLight,
    onPrimary = RailwayOnPrimaryLight,
    primaryContainer = RailwayPrimaryContainerLight,
    onPrimaryContainer = Color(0xFF001A41),
    secondary = RailwaySecondaryLight,
    onSecondary = RailwayOnSecondaryLight,
    secondaryContainer = MapBlueLight,
    onSecondaryContainer = RailwayBackgroundLight,
    tertiary = RailwayTertiaryLight,
    onTertiary = RailwayOnTertiaryLight,
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E0011),
    background = RailwayBackgroundLight,
    onBackground = RailwayOnSurfaceLight,
    surface = RailwaySurfaceLight,
    onSurface = RailwayOnSurfaceLight,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = RailwayOnSurfaceLight.copy(alpha = 0.6f),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val HackerColorScheme = darkColorScheme(
    primary = HackerGreen,
    onPrimary = Color.Black,
    primaryContainer = HackerGreenDark,
    onPrimaryContainer = HackerGreen,
    secondary = HackerGreen,
    onSecondary = Color.Black,
    secondaryContainer = HackerGreenDark,
    onSecondaryContainer = HackerGreen,
    tertiary = HackerGreen,
    onTertiary = Color.Black,
    background = HackerBackground,
    onBackground = HackerGreen,
    surface = HackerSurface,
    onSurface = HackerOnSurface,
    surfaceVariant = HackerSurface,
    onSurfaceVariant = HackerGreen.copy(alpha = 0.8f),
    outline = HackerGreen.copy(alpha = 0.5f)
)

@Composable
fun RailwayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAdmin: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isAdmin -> HackerColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
