package com.tadiwanashe.tutictlabs.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light theme
private val LightColorScheme = lightColorScheme(
    primary = TutBlueLight,
    onPrimary = Color.White,
    secondary = TutYellowLight,
    onSecondary = TutTextDark,
    tertiary = TutRedLight,
    background = TutGreyLight,
    onBackground = TutTextDark,
    surface = Color.White,
    onSurface = TutTextDark
)

// Dark theme
private val DarkColorScheme = darkColorScheme(
    primary = TutBlueDark,
    onPrimary = TutTextLight,
    secondary = TutYellowDark,
    onSecondary = TutGreyDark,
    tertiary = TutRedDark,
    background = TutGreyDark,
    onBackground = TutTextLight,
    surface = Color(0xFF1E1E1E),
    onSurface = TutTextLight
)



@Composable
fun YourAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}