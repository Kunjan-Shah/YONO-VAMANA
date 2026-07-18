package com.digi.digi_vamana.vamanagame.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digi.digi_vamana.R
import com.digi.digi_vamana.vamanagame.theme.AppPurple
import com.digi.digi_vamana.vamanagame.theme.AppPurpleDark
import com.digi.digi_vamana.vamanagame.theme.AppPurpleLight
import com.digi.digi_vamana.vamanagame.theme.GoldCoin

/**
 * The recurring avatar + speech-bubble + coin badge + bell header used across screens.
 * [bubbleContent] supplies the text/rich-text inside the speech bubble.
 */
@Composable
fun VamanaTopBar(
    coins: Int,
    modifier: Modifier = Modifier,
    trailingBadge: (@Composable () -> Unit)? = null,
    bubbleContent: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoinBadge(coins = coins)
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = AppPurple,
                modifier = Modifier.size(26.dp)
            )
        }
        if (trailingBadge != null) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                trailingBadge()
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AvatarWithBadge()
            Spacer(Modifier.width(14.dp))
            SpeechBubble(modifier = Modifier.weight(1f, fill = false), content = bubbleContent)
        }
    }
}

@Composable
fun AvatarWithBadge() {
    Image(
        painter = painterResource(R.drawable.vamana_mascot2),
        contentDescription = "VAMANA character",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .width(108.dp)
            .aspectRatio(360f / 441f)
    )
}

@Composable
private fun SpeechBubble(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(AppPurpleLight)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun CoinBadge(coins: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(AppPurple, AppPurpleDark)))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(GoldCoin),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = coins.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
