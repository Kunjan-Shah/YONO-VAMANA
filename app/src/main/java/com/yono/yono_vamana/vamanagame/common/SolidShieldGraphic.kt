package com.yono.yono_vamana.vamanagame.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A single-tone solid shield with a centered white glyph, used on mission-complete/checkpoint banners. */
@Composable
fun SolidShieldGraphic(
    tint: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}
