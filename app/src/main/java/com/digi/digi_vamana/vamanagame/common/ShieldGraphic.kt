package com.digi.digi_vamana.vamanagame.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.digi.digi_vamana.vamanagame.theme.AppPurple
import com.digi.digi_vamana.vamanagame.theme.GoldCoin
import com.digi.digi_vamana.vamanagame.theme.ShieldDark
import com.digi.digi_vamana.vamanagame.theme.ShieldLight

@Composable
fun ShieldGraphic(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldCoin,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopEnd)
        )
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = AppPurple.copy(alpha = 0.35f),
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopStart)
        )
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldCoin,
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.BottomEnd)
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .aspectRatio(0.85f)
        ) {
            val path = shieldPath(size)
            clipPath(path) {
                drawRect(color = ShieldLight, size = Size(size.width / 2f, size.height))
                drawRect(
                    color = ShieldDark,
                    topLeft = Offset(size.width / 2f, 0f),
                    size = Size(size.width / 2f, size.height)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

private fun shieldPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w * 0.5f, 0f)
        lineTo(w * 0.9f, h * 0.14f)
        cubicTo(w * 0.9f, h * 0.52f, w * 0.82f, h * 0.78f, w * 0.5f, h)
        cubicTo(w * 0.18f, h * 0.78f, w * 0.1f, h * 0.52f, w * 0.1f, h * 0.14f)
        close()
    }
}
