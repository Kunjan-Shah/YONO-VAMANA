package com.yono.yono_vamana.vamanagame.mission3

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GppGood
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.vamanagame.common.MissionBanner
import com.yono.yono_vamana.vamanagame.common.PointsGainedBadge
import com.yono.yono_vamana.vamanagame.common.ResultCircleGraphic
import com.yono.yono_vamana.vamanagame.common.ResultInfoCard
import com.yono.yono_vamana.vamanagame.common.SafetyTipDialog
import com.yono.yono_vamana.vamanagame.common.SolidShieldGraphic
import com.yono.yono_vamana.vamanagame.common.VamanaTopBar
import com.yono.yono_vamana.vamanagame.theme.ActionGreen
import com.yono.yono_vamana.vamanagame.theme.ActionRed
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.BadgeGreen
import com.yono.yono_vamana.vamanagame.theme.DangerRedBg
import com.yono.yono_vamana.vamanagame.theme.SafeGreenBg
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission3CompleteScreen(
    coins: Int = 650,
    pointsGained: Int = 100,
    onClaimCyberShield: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSafetyTip by remember { mutableStateOf(false) }
    if (showSafetyTip) {
        SafetyTipDialog(
            message = "Only give an app the permissions it truly needs!",
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
                text = "Well done!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "You removed the risky app and protected your private data.",
                fontSize = 16.sp,
                color = TextDark,
                lineHeight = 21.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 3 COMPLETE",
            badgeColor = BadgeGreen,
            title = "The Locker Key Test",
            titleColor = TextDark,
            subtitle = "Dangerous permissions refused",
            trailingGraphic = {
                SolidShieldGraphic(
                    tint = ActionGreen,
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
                text = "Risky app removed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your SMS and Accessibility access stay private.",
                fontSize = 15.sp,
                color = TextGray
            )
            Spacer(Modifier.height(24.dp))
            ResultCircleGraphic(
                icon = Icons.Filled.Delete,
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
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ActionRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Accessibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    title = "Permissions",
                    highlight = "Blocked",
                    highlightColor = ActionRed,
                    caption = ""
                )
                ResultInfoCard(
                    modifier = Modifier.weight(1f),
                    bgColor = SafeGreenBg,
                    icon = { MaliciousAppBadge() },
                    title = "Malicious App",
                    highlight = "Uninstalled",
                    highlightColor = ActionGreen,
                    caption = ""
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppPurple)
                .clickable(onClick = onClaimCyberShield)
                .padding(vertical = 18.dp)
        ) {
            Text(
                text = "Claim Cyber Shield",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Filled.GppGood,
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
            text = "Only give an app the permissions it truly needs.",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MaliciousAppBadge() {
    Box(modifier = Modifier.size(30.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(ActionGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.BugReport,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(13.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(ActionGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1600)
@Composable
private fun Mission3CompleteScreenPreview() {
    VAMANAGAMETheme {
        Mission3CompleteScreen()
    }
}
