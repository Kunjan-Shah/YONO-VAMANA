package com.yono.yono_vamana.vamanagame.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.yono.yono_vamana.vamanagame.common.ShieldGraphic
import com.yono.yono_vamana.vamanagame.common.VamanaTopBar
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.AppPurpleLight
import com.yono.yono_vamana.vamanagame.theme.ArrowGreenBg
import com.yono.yono_vamana.vamanagame.theme.ArrowOrangeBg
import com.yono.yono_vamana.vamanagame.theme.ArrowPurpleBg
import com.yono.yono_vamana.vamanagame.theme.BadgeGreen
import com.yono.yono_vamana.vamanagame.theme.BadgeOrange
import com.yono.yono_vamana.vamanagame.theme.BadgePurple
import com.yono.yono_vamana.vamanagame.theme.GoldCoin
import com.yono.yono_vamana.vamanagame.theme.MissionGreenEnd
import com.yono.yono_vamana.vamanagame.theme.MissionOrangeEnd
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun HomeScreen(
    coins: Int = 350,
    onMissionClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        VamanaTopBar(coins = coins) {
            Text(
                text = "Hello!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextDark, fontWeight = FontWeight.Bold)) {
                        append("I'm ")
                    }
                    withStyle(SpanStyle(color = AppPurple, fontWeight = FontWeight.Bold)) {
                        append("VAMANA")
                    }
                },
                fontSize = 19.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Your Security Guide",
                fontSize = 14.sp,
                color = TextGray
            )
        }
        Spacer(Modifier.height(24.dp))
        CyberShieldBanner()
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Your Missions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(Modifier.height(14.dp))
        MissionCard(
            iconRes = R.drawable.mission1_logo,
            missionLabel = "MISSION 1",
            badgeColor = BadgePurple,
            title = "The SMS\nBorder Checkpoint",
            subtitle = "Spot fake messages and protect yourself.",
            arrowBg = ArrowPurpleBg,
            arrowTint = AppPurple,
            onClick = { onMissionClick(1) }
        )
        Spacer(Modifier.height(16.dp))
        MissionCard(
            iconRes = R.drawable.mission2_logo,
            missionLabel = "MISSION 2",
            badgeColor = BadgeOrange,
            title = "Safe Door vs.\nShady Trapdoor",
            subtitle = "Block unknown app installs and stay safe.",
            arrowBg = ArrowOrangeBg,
            arrowTint = MissionOrangeEnd,
            onClick = { onMissionClick(2) },
            locked = true
        )
        Spacer(Modifier.height(16.dp))
        MissionCard(
            iconRes = R.drawable.mission3_logo,
            missionLabel = "MISSION 3",
            badgeColor = BadgeGreen,
            title = "The Locker\nKey Test",
            subtitle = "Check app permissions and guard your data.",
            arrowBg = ArrowGreenBg,
            arrowTint = MissionGreenEnd,
            onClick = { onMissionClick(3) },
            locked = true
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CyberShieldBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppPurpleLight)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextDark)) { append("Complete all ") }
                    withStyle(SpanStyle(color = AppPurple, fontWeight = FontWeight.Bold)) { append("3 missions") }
                    withStyle(SpanStyle(color = TextDark)) { append(" to earn your") }
                },
                fontSize = 19.sp,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Cyber Shield!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppPurple
            )
        }
        Spacer(Modifier.width(12.dp))
        ShieldGraphic(modifier = Modifier.size(110.dp))
    }
}

@Composable
private fun MissionCard(
    @DrawableRes iconRes: Int,
    missionLabel: String,
    badgeColor: Color,
    title: String,
    subtitle: String,
    arrowBg: Color,
    arrowTint: Color,
    onClick: () -> Unit,
    locked: Boolean = false
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
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
                .clickable(enabled = !locked, onClick = onClick)
                .alpha(if (locked) 0.45f else 1f)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = missionLabel,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextGray,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(arrowBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open mission",
                    tint = arrowTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (locked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GoldCoin)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1100)
@Composable
private fun HomeScreenPreview() {
    VAMANAGAMETheme {
        HomeScreen()
    }
}
