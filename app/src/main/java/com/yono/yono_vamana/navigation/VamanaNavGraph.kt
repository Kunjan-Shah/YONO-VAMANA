package com.yono.yono_vamana.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yono.yono_vamana.ui.dashboard.DashboardScreen
import com.yono.yono_vamana.ui.detail.LayerDetailScreen
import com.yono.yono_vamana.ui.intercept.SmsInterceptedScreen
import com.yono.yono_vamana.ui.navigation.VamanaBottomNavBar
import com.yono.yono_vamana.ui.onboarding.SecuritySetupScreen
import com.yono.yono_vamana.ui.transact.PaymentScreen
import com.yono.yono_vamana.ui.transact.DummyContacts
import com.yono.yono_vamana.ui.transact.TransactContactsScreen
import com.yono.yono_vamana.vamana.model.VamanaLayerId
import com.yono.yono_vamana.vamana.model.VamanaLayerRegistry
import com.yono.yono_vamana.vamanagame.VamanaGameFlow

private val TOP_LEVEL_ROUTES = setOf(VamanaDestination.Dashboard.route, VamanaDestination.Transact.route)

@Composable
fun VamanaNavGraph(
    startDestination: String,
    onSetupComplete: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                VamanaBottomNavBar(
                    currentRoute = currentRoute,
                    onVamanaSelected = {
                        navController.navigate(VamanaDestination.Dashboard.route) {
                            popUpTo(VamanaDestination.Dashboard.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onTransactSelected = {
                        navController.navigate(VamanaDestination.Transact.route) {
                            popUpTo(VamanaDestination.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
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
            ) { entry ->
                val layerIdArg = entry.arguments?.getString(VamanaDestination.LayerDetail.ARG_LAYER_ID)
                val layerId = layerIdArg?.let { runCatching { VamanaLayerId.valueOf(it) }.getOrNull() }
                val layerInfo = layerId?.let { VamanaLayerRegistry.find(it) } ?: VamanaLayerRegistry.layers.first()

                LayerDetailScreen(
                    layerInfo = layerInfo,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(VamanaDestination.Transact.route) {
                TransactContactsScreen(
                    onContactSelected = { contact ->
                        navController.navigate(VamanaDestination.Payment.createRoute(contact.id))
                    }
                )
            }

            composable(
                route = VamanaDestination.Payment.route,
                arguments = listOf(navArgument(VamanaDestination.Payment.ARG_CONTACT_ID) {
                    type = NavType.StringType
                })
            ) { entry ->
                val contact = DummyContacts.find(entry.arguments?.getString(VamanaDestination.Payment.ARG_CONTACT_ID))
                PaymentScreen(
                    contact = contact,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(VamanaDestination.SmsIntercepted.route) {
                SmsInterceptedScreen(
                    onLearnMore = {
                        navController.navigate(VamanaDestination.VamanaGame.route)
                    }
                )
            }

            composable(VamanaDestination.VamanaGame.route) {
                VamanaGameFlow(
                    onReturnToBankingApp = {
                        navController.navigate(VamanaDestination.Dashboard.route) {
                            popUpTo(VamanaDestination.Dashboard.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
