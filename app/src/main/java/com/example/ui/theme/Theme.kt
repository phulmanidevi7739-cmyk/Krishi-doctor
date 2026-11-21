package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage

private val LightColorScheme = lightColorScheme(
    primary = KrishiGreenPrimary,
    onPrimary = KrishiOnPrimary,
    primaryContainer = KrishiPrimaryContainer,
    onPrimaryContainer = KrishiOnPrimaryContainer,
    secondary = KrishiSecondary,
    onSecondary = KrishiOnSecondary,
    secondaryContainer = KrishiSecondaryContainer,
    onSecondaryContainer = KrishiOnSecondaryContainer,
    tertiary = KrishiAmberWarning,
    tertiaryContainer = KrishiAmberContainer,
    onTertiaryContainer = KrishiOnAmberContainer,
    background = KrishiBackground,
    onBackground = KrishiOnBackground,
    surface = KrishiSurface,
    onSurface = KrishiOnSurface,
    surfaceVariant = KrishiSurfaceVariant,
    onSurfaceVariant = KrishiOnSurfaceVariant,
    outline = KrishiOutline,
    error = KrishiError,
    errorContainer = KrishiErrorContainer,
    onErrorContainer = KrishiOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = KrishiDarkPrimary,
    onPrimary = KrishiDarkOnPrimary,
    primaryContainer = KrishiDarkPrimaryContainer,
    onPrimaryContainer = KrishiDarkOnPrimaryContainer,
    secondary = KrishiDarkPrimary,
    onSecondary = KrishiDarkOnPrimary,
    secondaryContainer = KrishiDarkSurfaceVariant,
    onSecondaryContainer = KrishiDarkOnSurface,
    tertiaryContainer = KrishiAmberContainer,
    onTertiaryContainer = KrishiOnAmberContainer,
    background = KrishiDarkBackground,
    onBackground = KrishiDarkOnSurface,
    surface = KrishiDarkSurface,
    onSurface = KrishiDarkOnSurface,
    surfaceVariant = KrishiDarkSurfaceVariant,
    onSurfaceVariant = KrishiDarkOnSurfaceVariant,
    outline = KrishiDarkOutline,
    error = KrishiError,
    errorContainer = KrishiErrorContainer,
    onErrorContainer = KrishiOnErrorContainer
)

val KrishiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun KrishiDoctorTheme(
    language: AppLanguage = AppLanguage.HINDI,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = krishiTypography(language),
        shapes = KrishiShapes,
        content = content
    )
}
