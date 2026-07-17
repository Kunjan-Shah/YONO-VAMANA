package com.yono.yono_vamana.vamanagame.mission1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.R
import com.yono.yono_vamana.vamanagame.common.MissionBanner
import com.yono.yono_vamana.vamanagame.common.PointsGainedBadge
import com.yono.yono_vamana.vamanagame.common.SafetyTipDialog
import com.yono.yono_vamana.vamanagame.common.VamanaTopBar
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.AppPurpleLight
import com.yono.yono_vamana.vamanagame.theme.GoldCoin
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission1CompleteScreen(
    coins: Int = 450,
    pointsGained: Int = 100,
    missionsComplete: Int = 1,
    totalMissions: Int = 3,
    onContinue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSafetyTip by remember { mutableStateOf(false) }
    if (showSafetyTip) {
        SafetyTipDialog(
            message = "Do not click on links in SMS from unknown senders!",
            onDismiss = { showSafetyTip = false }
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        VamanaTopBar(
            coins = coins,
            trailingBadge = { PointsGainedBadge(points = pointsGained) }
        ) {
            Text(
                text = "Great job! This message is fake. You spotted the warning signs and blocked it.",
                fontSize = 17.sp,
                color = TextDark,
                lineHeight = 23.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 1 COMPLETE",
            title = "The SMS Border Checkpoint"
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.m1_complete__banner),
                contentDescription = "Fake SMS blocked and shredded",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(759f / 557f)
            )
        }
//        Spacer(Modifier.height(20.dp))
//        StatsRow(
//            pointsGained = pointsGained,
//            missionsComplete = missionsComplete,
//            totalMissions = totalMissions
//        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppPurple)
                .clickable(onClick = onContinue)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, AppPurple, RoundedCornerShape(20.dp))
                .clickable(onClick = { showSafetyTip = true })
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Review Safety Tip",
                color = AppPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

//@Composable
//private fun StatsRow(pointsGained: Int, missionsComplete: Int, totalMissions: Int) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(20.dp))
//            .background(AppPurpleLight)
//            .padding(vertical = 18.dp, horizontal = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        StatItem(
//            icon = {
//                Icon(
//                    imageVector = Icons.Filled.GppGood,
//                    contentDescription = null,
//                    tint = AppPurple,
//                    modifier = Modifier.size(28.dp)
//                )
//            },
//            boldText = "Blocked",
//            grayText = "fake SMS",
//            modifier = Modifier.weight(1f)
//        )
//        StatDivider()
//        StatItem(
//            icon = {
//                Box(
//                    modifier = Modifier
//                        .size(26.dp)
//                        .clip(CircleShape)
//                        .background(GoldCoin),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Star,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(14.dp)
//                    )
//                }
//            },
//            boldText = "+$pointsGained",
//            grayText = "Points",
//            modifier = Modifier.weight(1f)
//        )
//        StatDivider()
//        StatItem(
//            icon = {
//                Icon(
//                    imageVector = Icons.Filled.TrackChanges,
//                    contentDescription = null,
//                    tint = AppPurple,
//                    modifier = Modifier.size(28.dp)
//                )
//            },
//            boldText = "$missionsComplete / $totalMissions",
//            grayText = "Missions Complete",
//            modifier = Modifier.weight(1f)
//        )
//    }
//}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(AppPurple.copy(alpha = 0.15f))
    )
}

@Composable
private fun StatItem(
    icon: @Composable () -> Unit,
    boldText: String,
    grayText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                text = boldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 16.sp
            )
            Text(
                text = grayText,
                fontSize = 11.sp,
                color = TextGray,
                lineHeight = 13.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1500)
@Composable
private fun Mission1CompleteScreenPreview() {
    VAMANAGAMETheme {
        Mission1CompleteScreen()
    }
}
