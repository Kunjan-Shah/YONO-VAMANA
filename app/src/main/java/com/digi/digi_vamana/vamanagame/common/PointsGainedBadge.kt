package com.digi.digi_vamana.vamanagame.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digi.digi_vamana.vamanagame.theme.AppPurple
import com.digi.digi_vamana.vamanagame.theme.AppPurpleLight

/** The small "+100" pill shown under the coin badge on mission-complete screens. */
@Composable
fun PointsGainedBadge(points: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppPurpleLight)
            .border(1.dp, AppPurple.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "+$points",
            color = AppPurple,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
