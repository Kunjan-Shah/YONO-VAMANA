package com.yono.yono_vamana.vamanagame.mission3

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.R
import com.yono.yono_vamana.vamanagame.common.IncorrectChoiceDialog
import com.yono.yono_vamana.vamanagame.common.MissionBanner
import com.yono.yono_vamana.vamanagame.common.SolidShieldGraphic
import com.yono.yono_vamana.vamanagame.common.VamanaTopBar
import com.yono.yono_vamana.vamanagame.theme.ActionGreen
import com.yono.yono_vamana.vamanagame.theme.ActionRed
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.AppPurple
import com.yono.yono_vamana.vamanagame.theme.AppPurpleLight
import com.yono.yono_vamana.vamanagame.theme.BadgeGreen
import com.yono.yono_vamana.vamanagame.theme.DangerRedBg
import com.yono.yono_vamana.vamanagame.theme.DashedDivider
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.TextGray
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission3CheckpointScreen(
    coins: Int = 550,
    onUninstallApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showIncorrectDialog by remember { mutableStateOf(false) }
    if (showIncorrectDialog) {
        IncorrectChoiceDialog(
            message = "Beware! Think twice before clicking 'Allow Permission'! Give permissions only to trusted apps",
            onDismiss = { showIncorrectDialog = false }
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        VamanaTopBar(coins = coins) {
            Text(
                text = "This app is asking for risky permissions. A calculator should not read your SMS or control your phone.",
                fontSize = 17.sp,
                color = TextDark,
                lineHeight = 23.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 3",
            badgeColor = BadgeGreen,
            title = "The Locker Key Test",
            titleColor = TextDark,
            subtitle = "Refuse dangerous app permissions",
            trailingGraphic = {
                SolidShieldGraphic(
                    tint = ActionGreen,
                    icon = Icons.Filled.Lock,
                    modifier = Modifier.size(90.dp)
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
                text = "Permission Request",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppPurpleLight)
                        .border(1.dp, AppPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.calculator),
                        contentDescription = "Calculator Pro",
                        modifier = Modifier.size(62.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextDark)) {
                                append("Calculator Pro")
                            }
                            withStyle(SpanStyle(color = TextDark)) {
                                append(" is requesting access to:")
                            }
                        },
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
//                    Spacer(Modifier.height(10.dp))
//                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        PermissionTag(icon = Icons.Filled.ChatBubble, label = "Read SMS")
//                        PermissionTag(icon = Icons.Filled.Accessibility, label = "Accessibility")
//                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Available keys",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KeyCard(icon = Icons.Filled.ChatBubble, label = "SMS", danger = true, modifier = Modifier.weight(1f))
                KeyCard(icon = Icons.Filled.Person, label = "Contacts", danger = false, modifier = Modifier.weight(1f))
                KeyCard(icon = Icons.Filled.PhotoCamera, label = "Camera", danger = false, modifier = Modifier.weight(1f))
                KeyCard(
                    icon = Icons.Filled.Accessibility,
                    label = "Accessibility",
                    danger = true,
                    modifier = Modifier.weight(1f),
                    labelFontSize = 9.sp
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldWarningIcon(modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Choose the safe action.",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, AppPurple, RoundedCornerShape(20.dp))
                    .clickable(onClick = { showIncorrectDialog = true })
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Give Keys",
                    color = AppPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ActionRed)
                    .clickable(onClick = onUninstallApp)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Uninstall App",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionTag(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(DangerRedBg)
            .border(1.5.dp, ActionRed, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(ActionRed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = ActionRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun KeyCard(
    icon: ImageVector,
    label: String,
    danger: Boolean,
    modifier: Modifier = Modifier,
    labelFontSize: TextUnit = 12.sp
) {
    val bg = if (danger) DangerRedBg else AppPurpleLight
    val borderColor = if (danger) ActionRed else DashedDivider
    val labelColor = if (danger) ActionRed else TextDark

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (danger) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ActionRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppPurple,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = labelFontSize,
            color = labelColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ShieldWarningIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = AppPurple,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "!",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1600)
@Composable
private fun Mission3CheckpointScreenPreview() {
    VAMANAGAMETheme {
        Mission3CheckpointScreen()
    }
}
