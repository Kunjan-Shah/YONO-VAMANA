package com.digi.digi_vamana.vamanagame.mission2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PublicOff
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digi.digi_vamana.R
import com.digi.digi_vamana.vamanagame.common.MissionBanner
import com.digi.digi_vamana.vamanagame.common.PointsGainedBadge
import com.digi.digi_vamana.vamanagame.common.ResultCircleGraphic
import com.digi.digi_vamana.vamanagame.common.ResultInfoCard
import com.digi.digi_vamana.vamanagame.common.SafetyTipDialog
import com.digi.digi_vamana.vamanagame.common.SolidShieldGraphic
import com.digi.digi_vamana.vamanagame.common.VamanaTopBar
import com.digi.digi_vamana.vamanagame.theme.ActionGreen
import com.digi.digi_vamana.vamanagame.theme.ActionRed
import com.digi.digi_vamana.vamanagame.theme.AppBackground
import com.digi.digi_vamana.vamanagame.theme.AppPurple
import com.digi.digi_vamana.vamanagame.theme.BadgeOrange
import com.digi.digi_vamana.vamanagame.theme.DangerRedBg
import com.digi.digi_vamana.vamanagame.theme.SafeGreenBg
import com.digi.digi_vamana.vamanagame.theme.TextDark
import com.digi.digi_vamana.vamanagame.theme.TextGray
import com.digi.digi_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission2CompleteScreen(
    coins: Int = 550,
    pointsGained: Int = 100,
    onContinue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSafetyTip by remember { mutableStateOf(false) }
    if (showSafetyTip) {
        SafetyTipDialog(
            message = "Install banking apps only from Play Store. Never allow installs from unknown sources",
            onDismiss = { showSafetyTip = false }
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        VamanaTopBar(
            coins = coins,
            trailingBadge = { PointsGainedBadge(points = pointsGained) }
        ) {
            Text(
                text = "Excellent!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "You denied the unsafe install and kept your phone secure.",
                fontSize = 16.sp,
                color = TextDark,
                lineHeight = 21.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 2 COMPLETE",
            badgeColor = BadgeOrange,
            title = "Safe Door vs. Shady Trapdoor",
            titleColor = TextDark,
            trailingGraphic = {
                SolidShieldGraphic(
                    tint = AppPurple,
                    icon = Icons.Filled.Check,
                    modifier = Modifier.size(72.dp)
                )
            }
        )
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
            Text(
                text = "Unsafe app install blocked",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You chose Deny — the safe action.",
                fontSize = 15.sp,
                color = TextGray
            )
            Spacer(Modifier.height(24.dp))
            ResultCircleGraphic(
                icon = Icons.Filled.Lock,
                tint = ActionGreen,
                haloTint = SafeGreenBg
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ResultInfoCard(
                    modifier = Modifier.weight(1f),
                    bgColor = DangerRedBg,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PublicOff,
                            contentDescription = null,
                            tint = ActionRed,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = "Unknown source",
                    highlight = "Blocked",
                    highlightColor = ActionRed,
                    caption = "No APK installed"
                )
                ResultInfoCard(
                    modifier = Modifier.weight(1f),
                    bgColor = SafeGreenBg,
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.play_store_logo),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = "Official store",
                    highlight = "Use only trusted",
                    highlightColor = ActionGreen,
                    caption = "app stores"
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppPurple)
                .clickable(onClick = onContinue)
                .padding(vertical = 18.dp)
        ) {
            Text(
                text = "Continue to Mission 3",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(22.dp)
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
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Never allow installs from unknown sources.",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1600)
@Composable
private fun Mission2CompleteScreenPreview() {
    VAMANAGAMETheme {
        Mission2CompleteScreen()
    }
}
