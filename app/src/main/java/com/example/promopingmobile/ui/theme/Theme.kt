package com.example.promopingmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = OrangeLight,
    onSecondary = Ink,
    background = Color(0xFF111111),
    onBackground = Color.White,
    surface = Color(0xFF1C1C1C),
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = OrangeDark,
    onSecondary = Color.White,
    tertiary = OrangeLight,
    background = Color(0xFFF9F9F9),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    outline = Border
)

@Composable
fun PromoPingMobileTheme(
    darkTheme: Boolean = false,
    // Mantém a paleta de marca mesmo em Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}