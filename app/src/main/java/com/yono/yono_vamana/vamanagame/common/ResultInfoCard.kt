package com.yono.yono_vamana.vamanagame.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray

/** A tinted result tile (icon + title + colored highlight + caption), used on mission-complete screens. */
@Composable
fun ResultInfoCard(
    modifier: Modifier = Modifier,
    bgColor: Color,
    icon: @Composable () -> Unit,
    title: String,
    highlight: String,
    highlightColor: Color,
    caption: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = highlight, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = highlightColor)
            Text(text = caption, fontSize = 12.sp, color = TextGray)
        }
    }
}
