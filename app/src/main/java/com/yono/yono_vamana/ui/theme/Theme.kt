package com.yono.yono_vamana.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YonoDarkColorScheme = darkColorScheme(
    primary = YonoPurpleLight,
    onPrimary = YonoNeutral10,
    primaryContainer = YonoPurpleDark,
    onPrimaryContainer = YonoNeutral95,
    secondary = YonoOrange,
    onSecondary = YonoNeutral10,
    secondaryContainer = YonoOrangeDark,
    onSecondaryContainer = YonoNeutral95,
    tertiary = YonoYellow,
    onTertiary = YonoNeutral10,
    background = YonoNeutral10,
    onBackground = YonoNeutral95,
    surface = Color(0xFF241A33),
    onSurface = YonoNeutral95,
    surfaceVariant = YonoPurpleDark,
    onSurfaceVariant = YonoNeutral90,
    error = YonoRedAlert,
    onError = YonoNeutral99
)

private val YonoLightColorScheme = lightColorScheme(
    primary = YonoPurple,
    onPrimary = YonoNeutral99,
    primaryContainer = YonoPurpleSurfaceTint,
    onPrimaryContainer = YonoPurpleDarkest,
    secondary = YonoOrange,
    onSecondary = YonoNeutral99,
    secondaryContainer = Color(0xFFFFE3C2),
    onSecondaryContainer = YonoOrangeDark,
    tertiary = YonoYellow,
    onTertiary = YonoNeutral10,
    background = YonoNeutral90,
    onBackground = YonoNeutral10,
    surface = YonoNeutral99,
    onSurface = YonoNeutral10,
    surfaceVariant = YonoPurpleSurfaceTint,
    onSurfaceVariant = YonoNeutral30,
    outline = YonoNeutral50,
    error = YonoRedAlert,
    onError = YonoNeutral99
)

@Composable
fun YONOVAMANATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YonoDarkColorScheme else YonoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
