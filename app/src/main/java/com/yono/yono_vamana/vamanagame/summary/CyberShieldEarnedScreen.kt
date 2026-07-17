package com.yono.yono_vamana.vamanagame.summary

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.R
import com.yono.yono_vamana.vamanagame.common.CoinBadge
import com.yono.yono_vamana.vamanagame.theme.ActionGreen
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.AppPurpleLight
import com.yono.yono_vamana.vamanagame.theme.BadgeGreen
import com.yono.yono_vamana.vamanagame.theme.SafeGreenBg
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun CyberShieldEarnedScreen(
    coins: Int = 650,
    missionsComplete: Int = 3,
    totalMissions: Int = 3,
    onReviewSafetyTips: () -> Unit = {},
    onReturnToBankingApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
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
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.vamana_congratulates),
                contentDescription = "VAMANA congratulates you",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(170.dp)
                    .aspectRatio(425f / 570f)
            )
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(AppPurpleLight)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Congratulations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPurple
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "You have earned your Cyber Shield.",
                    fontSize = 16.sp,
                    color = TextDark,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppPurple.copy(alpha = 0.15f))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "These safe habits will help protect you from fraud every day.",
                    fontSize = 14.sp,
                    color = TextGray,
                    lineHeight = 19.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(
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
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BadgeGreen)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "TRAINING COMPLETE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextDark)) {
                                append("$missionsComplete / $totalMissions ")
                            }
                            withStyle(SpanStyle(color = TextDark)) {
                                append("Missions Completed")
                            }
                        },
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppPurpleLight)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.trophy),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(text = "Total Points Earned", fontSize = 13.sp, color = TextGray)
                            Text(
                                text = coins.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppPurple
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(R.drawable.cybershield),
                    contentDescription = "Cyber Shield badge",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(364f / 396f)
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MissionCompleteTile(label = "Mission 1", modifier = Modifier.weight(1f))
                VerticalTileDivider()
                MissionCompleteTile(label = "Mission 2", modifier = Modifier.weight(1f))
                VerticalTileDivider()
                MissionCompleteTile(label = "Mission 3", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppPurpleLight)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ActionGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GppGood,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Block fake messages, deny unknown installs, and refuse risky permissions.",
                    fontSize = 14.sp,
                    color = TextDark,
                    lineHeight = 19.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(20.dp))
//                .border(1.5.dp, AppPurple, RoundedCornerShape(20.dp))
//                .clickable(onClick = onReviewSafetyTips)
//                .padding(vertical = 16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "Review Safety Tips",
//                color = AppPurple,
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp
//            )
//        }
//        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppPurple)
                .clickable(onClick = onReturnToBankingApp)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Return to banking app",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MissionCompleteTile(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SafeGreenBg)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(ActionGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
        Text(text = "Completed", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ActionGreen)
    }
}

@Composable
private fun VerticalTileDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(AppPurple.copy(alpha = 0.1f))
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1700)
@Composable
private fun CyberShieldEarnedScreenPreview() {
    VAMANAGAMETheme {
        CyberShieldEarnedScreen()
    }
}
