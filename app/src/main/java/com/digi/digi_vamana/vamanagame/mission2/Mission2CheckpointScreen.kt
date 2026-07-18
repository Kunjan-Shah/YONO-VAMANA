package com.digi.digi_vamana.vamanagame.mission2

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digi.digi_vamana.R
import com.digi.digi_vamana.vamanagame.common.ActionButton
import com.digi.digi_vamana.vamanagame.common.IncorrectChoiceDialog
import com.digi.digi_vamana.vamanagame.common.MissionBanner
import com.digi.digi_vamana.vamanagame.common.VamanaTopBar
import com.digi.digi_vamana.vamanagame.theme.ActionGreen
import com.digi.digi_vamana.vamanagame.theme.ActionRed
import com.digi.digi_vamana.vamanagame.theme.AppBackground
import com.digi.digi_vamana.vamanagame.theme.AppPurple
import com.digi.digi_vamana.vamanagame.theme.AppPurpleLight
import com.digi.digi_vamana.vamanagame.theme.DangerRedBg
import com.digi.digi_vamana.vamanagame.theme.DashedDivider
import com.digi.digi_vamana.vamanagame.theme.SafeGreenBg
import com.digi.digi_vamana.vamanagame.theme.TextDark
import com.digi.digi_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission2CheckpointScreen(
    coins: Int = 450,
    onDenyInstall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showIncorrectDialog by remember { mutableStateOf(false) }
    if (showIncorrectDialog) {
        IncorrectChoiceDialog(
            message = "STOP! It is never safe to install apps from unknown sources!",
            onDismiss = { showIncorrectDialog = false }
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
        VamanaTopBar(coins = coins) {
            Text(
                text = "A link is trying to install an app. Close the trapdoor and choose Deny.",
                fontSize = 17.sp,
                color = TextDark,
                lineHeight = 23.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 2",
            title = "Safe Door vs. Shady Trapdoor",
            subtitle = "Block unknown app installs"
        )
        Spacer(Modifier.height(20.dp))
        TrapdoorScanCard()
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Choose the safe action",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppPurple,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ActionButton(
                label = "Allow Install",
                icon = Icons.Filled.GppGood,
                containerColor = ActionGreen,
                modifier = Modifier.weight(1f),
                onClick = { showIncorrectDialog = true }
            )
            ActionButton(
                label = "Deny Install",
                icon = Icons.Filled.Lock,
                containerColor = ActionRed,
                modifier = Modifier.weight(1f),
                onClick = onDenyInstall
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TrapdoorScanCard() {
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
            .background(AppPurpleLight)
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UrlPill()
        Spacer(Modifier.height(6.dp))
        DashedVerticalLine(height = 18.dp)
        ChromePermissionCard(modifier = Modifier.fillMaxWidth(0.88f))
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            SourceOption(
                modifier = Modifier.weight(1f),
                borderColor = ActionGreen,
                bgColor = SafeGreenBg,
                iconRes = R.drawable.play_store_source,
                label = "Official Store"
            )
            SourceOption(
                modifier = Modifier.weight(1f),
                borderColor = ActionRed,
                bgColor = DangerRedBg,
                iconRes = R.drawable.unknown_source,
                label = "Unknown Source",
                caption = ""
            )
        }
    }
}

@Composable
private fun UrlPill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, AppPurple.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = null,
            tint = AppPurple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "suspicious-site.com/app-download.apk",
            color = AppPurple,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DashedVerticalLine(height: Dp) {
    Canvas(
        modifier = Modifier
            .width(1.dp)
            .height(height)
    ) {
        drawLine(
            color = DashedDivider,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}

@Composable
private fun ChromePermissionCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.chrome_logo),
                contentDescription = "Chrome",
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Chrome requires permission to install apps from unknown sources. Allow?",
                fontSize = 14.sp,
                color = TextDark,
                lineHeight = 19.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedPillLabel(text = "Deny", color = ActionRed, modifier = Modifier.weight(1f))
            OutlinedPillLabel(text = "Allow", color = AppPurple, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun OutlinedPillLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.5.dp, color, RoundedCornerShape(50))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun SourceOption(
    modifier: Modifier = Modifier,
    borderColor: Color,
    bgColor: Color,
    @DrawableRes iconRes: Int,
    label: String,
    caption: String? = null
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(2.dp, borderColor, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        if (caption != null) {
//            Spacer(Modifier.height(6.dp))
//            Icon(
//                imageVector = Icons.Filled.ArrowUpward,
//                contentDescription = null,
//                tint = AppPurple,
//                modifier = Modifier.size(16.dp)
//            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = caption,
                color = AppPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1500)
@Composable
private fun Mission2CheckpointScreenPreview() {
    VAMANAGAMETheme {
        Mission2CheckpointScreen()
    }
}
