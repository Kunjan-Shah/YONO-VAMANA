package com.yono.yono_vamana.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.yono.yono_vamana.navigation.VamanaDestination
import com.yono.yono_vamana.ui.theme.YonoOrange
import com.yono.yono_vamana.ui.theme.YonoPurple

/** Bottom navigation shown on the two top-level tabs: VAMANA and Transact. */
@Composable
fun VamanaBottomNavBar(
    currentRoute: String?,
    onVamanaSelected: () -> Unit,
    onTransactSelected: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == VamanaDestination.Dashboard.route,
            onClick = onVamanaSelected,
            icon = { Icon(imageVector = Icons.Filled.Shield, contentDescription = null) },
            label = { Text("VAMANA") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YonoPurple,
                selectedTextColor = YonoPurple,
                indicatorColor = YonoPurple.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == VamanaDestination.Transact.route,
            onClick = onTransactSelected,
            icon = { Icon(imageVector = Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Transact") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YonoOrange,
                selectedTextColor = YonoOrange,
                indicatorColor = YonoOrange.copy(alpha = 0.15f)
            )
        )
    }
}
