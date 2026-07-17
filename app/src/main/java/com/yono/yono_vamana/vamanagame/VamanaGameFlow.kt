package com.yono.yono_vamana.vamanagame

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yono.yono_vamana.vamanagame.home.HomeScreen
import com.yono.yono_vamana.vamanagame.mission1.Mission1CheckpointScreen
import com.yono.yono_vamana.vamanagame.mission1.Mission1CompleteScreen
import com.yono.yono_vamana.vamanagame.mission2.Mission2CheckpointScreen
import com.yono.yono_vamana.vamanagame.mission2.Mission2CompleteScreen
import com.yono.yono_vamana.vamanagame.mission3.Mission3CheckpointScreen
import com.yono.yono_vamana.vamanagame.mission3.Mission3CompleteScreen
import com.yono.yono_vamana.vamanagame.summary.CyberShieldEarnedScreen

private enum class GameScreen {
    Home,
    Mission1Checkpoint,
    Mission1Complete,
    Mission2Checkpoint,
    Mission2Complete,
    Mission3Checkpoint,
    Mission3Complete,
    CyberShieldEarned
}

/**
 * Ported from VAMANAGAME's MainActivity — the same screen-switching flow
 * (Home -> Mission1 -> Mission2 -> Mission3 -> CyberShieldEarned), just
 * mounted as a composable inside YONO-VAMANA's own NavHost instead of a
 * separate Activity/Scaffold. The individual screens are unmodified copies
 * of VAMANAGAME's; only this flow-controller glue is new, replacing
 * VAMANAGAME's "reset to Home" exit with a real hand-off back to the host
 * banking app via [onReturnToBankingApp].
 */
@Composable
fun VamanaGameFlow(onReturnToBankingApp: () -> Unit, modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(GameScreen.Home) }
    BackHandler(enabled = screen != GameScreen.Home) {
        screen = GameScreen.Home
    }
    when (screen) {
        GameScreen.Home -> HomeScreen(
            modifier = modifier,
            onMissionClick = { missionNumber ->
                if (missionNumber == 1) screen = GameScreen.Mission1Checkpoint
            }
        )
        GameScreen.Mission1Checkpoint -> Mission1CheckpointScreen(
            modifier = modifier,
            onBlockMessage = { screen = GameScreen.Mission1Complete }
        )
        GameScreen.Mission1Complete -> Mission1CompleteScreen(
            modifier = modifier,
            onContinue = { screen = GameScreen.Mission2Checkpoint }
        )
        GameScreen.Mission2Checkpoint -> Mission2CheckpointScreen(
            modifier = modifier,
            onDenyInstall = { screen = GameScreen.Mission2Complete }
        )
        GameScreen.Mission2Complete -> Mission2CompleteScreen(
            modifier = modifier,
            onContinue = { screen = GameScreen.Mission3Checkpoint }
        )
        GameScreen.Mission3Checkpoint -> Mission3CheckpointScreen(
            modifier = modifier,
            onUninstallApp = { screen = GameScreen.Mission3Complete }
        )
        GameScreen.Mission3Complete -> Mission3CompleteScreen(
            modifier = modifier,
            onClaimCyberShield = { screen = GameScreen.CyberShieldEarned }
        )
        GameScreen.CyberShieldEarned -> CyberShieldEarnedScreen(
            modifier = modifier,
            onReturnToBankingApp = onReturnToBankingApp
        )
    }
}
