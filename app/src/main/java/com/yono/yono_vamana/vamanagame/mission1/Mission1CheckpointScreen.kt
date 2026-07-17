package com.yono.yono_vamana.vamanagame.mission1

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yono.yono_vamana.R
import com.yono.yono_vamana.vamanagame.common.ActionButton
import com.yono.yono_vamana.vamanagame.common.IncorrectChoiceDialog
import com.yono.yono_vamana.vamanagame.common.MissionBanner
import com.yono.yono_vamana.vamanagame.common.VamanaTopBar
import com.yono.yono_vamana.vamanagame.theme.ActionGreen
import com.yono.yono_vamana.vamanagame.theme.ActionRed
import com.yono.yono_vamana.vamanagame.theme.AppBackground
import com.yono.yono_vamana.vamanagame.theme.DashedDivider
import com.yono.yono_vamana.vamanagame.theme.TextDark
import com.yono.yono_vamana.vamanagame.theme.VAMANAGAMETheme

@Composable
fun Mission1CheckpointScreen(
    coins: Int = 350,
    onBlockMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showIncorrectDialog by remember { mutableStateOf(false) }
    if (showIncorrectDialog) {
        IncorrectChoiceDialog(
            message = "Wait! This message isn't real. Look at that weird link. Let's try blocking it instead!",
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
                text = "Let's inspect this message. Find the warning signs and block it.",
                fontSize = 17.sp,
                color = TextDark,
                lineHeight = 23.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        MissionBanner(
            badgeText = "MISSION 1",
            title = "The SMS Border Checkpoint",
            subtitle = "Spot fake messages"
        )
        Spacer(Modifier.height(20.dp))
        CheckpointScanCard()
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Choose the safe action",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ActionButton(
                label = "Block Message",
                icon = Icons.Filled.Delete,
                containerColor = ActionRed,
                modifier = Modifier.weight(1f),
                onClick = onBlockMessage
            )
            ActionButton(
                label = "Allow Message",
                icon = Icons.Filled.Check,
                containerColor = ActionGreen,
                modifier = Modifier.weight(1f),
                onClick = { showIncorrectDialog = true }
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CheckpointScanCard() {
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
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.vamana_m1_banner),
            contentDescription = "Checkpoint scan",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1002f / 404f)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(14.dp))
        DashedDividerLine()
        Spacer(Modifier.height(14.dp))
        Image(
            painter = painterResource(R.drawable.malicious_sms),
            contentDescription = "Suspicious SMS message under inspection",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(916f / 532f)
        )
    }
}

@Composable
private fun DashedDividerLine() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = DashedDivider,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1400)
@Composable
private fun Mission1CheckpointScreenPreview() {
    VAMANAGAMETheme {
        Mission1CheckpointScreen()
    }
}
