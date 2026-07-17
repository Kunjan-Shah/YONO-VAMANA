package com.yono.yono_vamana.vamanagame.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.AppPurpleLight
import com.yono.yono_vamana.vamanagame.theme.BadgePurple
import com.yono.yono_vamana.vamanagame.theme.TextGray

/** Purple mission badge + title (+ optional subtitle) + [ShieldGraphic], reused across mission screens. */
@Composable
fun MissionBanner(
    badgeText: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeColor: Color = BadgePurple,
    titleColor: Color = AppPurple,
    trailingGraphic: @Composable () -> Unit = { ShieldGraphic(modifier = Modifier.size(90.dp)) }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppPurpleLight)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = titleColor,
                lineHeight = 27.sp
            )
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    color = TextGray
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        trailingGraphic()
    }
}
