package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode
import com.example.data.model.ReadingFontSize

val LocalReadingFontScale = compositionLocalOf { 1.0f }

private val OrthodoxLightColorScheme = lightColorScheme(
    primary = OrthodoxRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE8E8),
    onPrimaryContainer = OrthodoxRedDark,
    secondary = OrthodoxGold,
    onSecondary = Color.White,
    secondaryContainer = OrthodoxGoldLight,
    onSecondaryContainer = Color(0xFF3B2A00),
    tertiary = OrthodoxTheotokosBlue,
    background = ParchmentBgLight,
    onBackground = TextSepiaDark,
    surface = ParchmentSurfaceLight,
    onSurface = TextSepiaDark,
    surfaceVariant = ParchmentSurfaceVariantLight,
    onSurfaceVariant = TextSepiaMedium,
    outline = Color(0xFFD3C5B7)
)

private val OrthodoxDarkColorScheme = darkColorScheme(
    primary = OrthodoxDarkPrimary,
    onPrimary = Color(0xFF4A0012),
    primaryContainer = Color(0xFF670B1B),
    onPrimaryContainer = Color(0xFFFFDADB),
    secondary = OrthodoxDarkGold,
    onSecondary = Color(0xFF382C00),
    secondaryContainer = Color(0xFF554300),
    onSecondaryContainer = Color(0xFFFFE08B),
    tertiary = Color(0xFF8AC5E8),
    background = OrthodoxDarkBg,
    onBackground = OrthodoxDarkText,
    surface = OrthodoxDarkSurface,
    onSurface = OrthodoxDarkText,
    surfaceVariant = OrthodoxDarkSurfaceVariant,
    onSurfaceVariant = OrthodoxDarkTextMuted,
    outline = Color(0xFF5B4F51)
)

private val OrthodoxParchmentColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF2D5CE),
    onPrimaryContainer = Color(0xFF3E080F),
    secondary = SepiaGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBD8B7),
    onSecondaryContainer = Color(0xFF332000),
    tertiary = Color(0xFF3A5A6C),
    background = SepiaBg,
    onBackground = SepiaText,
    surface = SepiaSurface,
    onSurface = SepiaText,
    surfaceVariant = SepiaSurfaceVariant,
    onSurfaceVariant = Color(0xFF594233),
    outline = Color(0xFFC7B89F)
)

@Composable
fun OrthodoxCalendarTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    readingFontSize: ReadingFontSize = ReadingFontSize.NORMAL,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (isSystemDark) OrthodoxDarkColorScheme else OrthodoxLightColorScheme
        AppThemeMode.LIGHT -> OrthodoxLightColorScheme
        AppThemeMode.DARK -> OrthodoxDarkColorScheme
        AppThemeMode.PARCHMENT -> OrthodoxParchmentColorScheme
    }

    CompositionLocalProvider(
        LocalReadingFontScale provides readingFontSize.scale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
