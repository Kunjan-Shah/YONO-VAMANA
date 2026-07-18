package com.digi.digi_vamana.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.digi.digi_vamana.navigation.VamanaDestination
import com.digi.digi_vamana.ui.theme.DigiOrange
import com.digi.digi_vamana.ui.theme.DigiPurple

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
                selectedIconColor = DigiPurple,
                selectedTextColor = DigiPurple,
                indicatorColor = DigiPurple.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == VamanaDestination.Transact.route,
            onClick = onTransactSelected,
            icon = { Icon(imageVector = Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Transact") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DigiOrange,
                selectedTextColor = DigiOrange,
                indicatorColor = DigiOrange.copy(alpha = 0.15f)
            )
        )
    }
}
