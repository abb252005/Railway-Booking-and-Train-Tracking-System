package com.example.railway.ui.theme

//import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RailwayPrimaryDark,
    onPrimary = RailwayOnPrimaryDark,
    primaryContainer = RailwayPrimaryContainerDark,
    onPrimaryContainer = RailwayOnPrimaryContainerDark,
    secondary = RailwaySecondaryDark,
    onSecondary = RailwayOnSecondaryDark,
    secondaryContainer = RailwaySecondaryContainerDark,
    onSecondaryContainer = RailwayOnSecondaryContainerDark,
    tertiary = RailwayTertiaryDark,
    onTertiary = RailwayOnTertiaryDark,
    tertiaryContainer = RailwayTertiaryContainerDark,
    onTertiaryContainer = RailwayOnTertiaryContainerDark,
    background = RailwayBackgroundDark,
    onBackground = RailwayOnBackgroundDark,
    surface = RailwaySurfaceDark,
    onSurface = RailwayOnSurfaceDark,
    error = RailwayErrorDark,
    onError = RailwayOnErrorDark,
    errorContainer = RailwayErrorContainerDark,
    onErrorContainer = RailwayOnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = RailwayPrimaryLight,
    onPrimary = RailwayOnPrimaryLight,
    primaryContainer = RailwayPrimaryContainerLight,
    onPrimaryContainer = RailwayOnPrimaryContainerLight,
    secondary = RailwaySecondaryLight,
    onSecondary = RailwayOnSecondaryLight,
    secondaryContainer = RailwaySecondaryContainerLight,
    onSecondaryContainer = RailwayOnSecondaryContainerLight,
    tertiary = RailwayTertiaryLight,
    onTertiary = RailwayOnTertiaryLight,
    tertiaryContainer = RailwayTertiaryContainerLight,
    onTertiaryContainer = RailwayOnTertiaryContainerLight,
    background = RailwayBackgroundLight,
    onBackground = RailwayOnBackgroundLight,
    surface = RailwaySurfaceLight,
    onSurface = RailwayOnSurfaceLight,
    error = RailwayErrorLight,
    onError = RailwayOnErrorLight,
    errorContainer = RailwayErrorContainerLight,
    onErrorContainer = RailwayOnErrorContainerLight
)

@Composable
fun RailwayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set as false to prioritize our vibrant palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
