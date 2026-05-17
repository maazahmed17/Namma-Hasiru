package com.example.nammahasiru

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nammahasiru.ui.navigation.NammaHasiruNavHost
import com.example.nammahasiru.ui.navigation.Routes
import com.example.nammahasiru.ui.navigation.navigateToTab

@Composable
fun NammaHasiruApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = when {
        currentRoute == null -> false
        currentRoute == Routes.NEW_PLANT -> false
        currentRoute == Routes.REMINDERS -> false
        currentRoute.startsWith("${Routes.STATUS_UPDATE}/") -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = { navController.navigateToTab(Routes.HOME) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.MAP,
                        onClick = { navController.navigateToTab(Routes.MAP) },
                        icon = { Icon(Icons.Filled.Place, contentDescription = "Map") },
                        label = { Text("Map") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SPECIES,
                        onClick = { navController.navigateToTab(Routes.SPECIES) },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Species") },
                        label = { Text("Species") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = { navController.navigateToTab(Routes.PROFILE) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                    )
                }
            }
        },
    ) { paddingValues ->
        NammaHasiruNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
