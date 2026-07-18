package com.digi.digi_vamana.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DigiDarkColorScheme = darkColorScheme(
    primary = DigiPurpleLight,
    onPrimary = DigiNeutral10,
    primaryContainer = DigiPurpleDark,
    onPrimaryContainer = DigiNeutral95,
    secondary = DigiOrange,
    onSecondary = DigiNeutral10,
    secondaryContainer = DigiOrangeDark,
    onSecondaryContainer = DigiNeutral95,
    tertiary = DigiYellow,
    onTertiary = DigiNeutral10,
    background = DigiNeutral10,
    onBackground = DigiNeutral95,
    surface = Color(0xFF241A33),
    onSurface = DigiNeutral95,
    surfaceVariant = DigiPurpleDark,
    onSurfaceVariant = DigiNeutral90,
    error = DigiRedAlert,
    onError = DigiNeutral99
)

private val DigiLightColorScheme = lightColorScheme(
    primary = DigiPurple,
    onPrimary = DigiNeutral99,
    primaryContainer = DigiPurpleSurfaceTint,
    onPrimaryContainer = DigiPurpleDarkest,
    secondary = DigiOrange,
    onSecondary = DigiNeutral99,
    secondaryContainer = Color(0xFFFFE3C2),
    onSecondaryContainer = DigiOrangeDark,
    tertiary = DigiYellow,
    onTertiary = DigiNeutral10,
    background = DigiNeutral90,
    onBackground = DigiNeutral10,
    surface = DigiNeutral99,
    onSurface = DigiNeutral10,
    surfaceVariant = DigiPurpleSurfaceTint,
    onSurfaceVariant = DigiNeutral30,
    outline = DigiNeutral50,
    error = DigiRedAlert,
    onError = DigiNeutral99
)

@Composable
fun DigiVamanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DigiDarkColorScheme else DigiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
