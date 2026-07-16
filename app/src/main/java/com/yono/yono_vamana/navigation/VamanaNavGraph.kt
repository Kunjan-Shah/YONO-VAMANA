package com.yono.yono_vamana.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yono.yono_vamana.ui.dashboard.DashboardScreen
import com.yono.yono_vamana.ui.detail.LayerDetailScreen
import com.yono.yono_vamana.ui.onboarding.SecuritySetupScreen
import com.yono.yono_vamana.vamana.model.VamanaLayerId
import com.yono.yono_vamana.vamana.model.VamanaLayerRegistry

@Composable
fun VamanaNavGraph(
    startDestination: String,
    onSetupComplete: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(VamanaDestination.SecuritySetup.route) {
            SecuritySetupScreen(
                onSetupComplete = {
                    onSetupComplete()
                    navController.navigate(VamanaDestination.Dashboard.route) {
                        popUpTo(VamanaDestination.SecuritySetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(VamanaDestination.Dashboard.route) {
            DashboardScreen(
                onLayerSelected = { layerId ->
                    navController.navigate(VamanaDestination.LayerDetail.createRoute(layerId.name))
                }
            )
        }

        composable(
            route = VamanaDestination.LayerDetail.route,
            arguments = listOf(navArgument(VamanaDestination.LayerDetail.ARG_LAYER_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val layerIdArg = backStackEntry.arguments?.getString(VamanaDestination.LayerDetail.ARG_LAYER_ID)
            val layerId = layerIdArg?.let { runCatching { VamanaLayerId.valueOf(it) }.getOrNull() }
            val layerInfo = layerId?.let { VamanaLayerRegistry.find(it) } ?: VamanaLayerRegistry.layers.first()

            LayerDetailScreen(
                layerInfo = layerInfo,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
