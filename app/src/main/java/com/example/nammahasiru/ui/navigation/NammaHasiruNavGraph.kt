package com.example.nammahasiru.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.nammahasiru.ui.dashboard.DashboardScreen
import com.example.nammahasiru.ui.newplant.NewPlantScreen
import com.example.nammahasiru.ui.profile.ProfileScreen
import com.example.nammahasiru.ui.reminders.RemindersScreen
import com.example.nammahasiru.ui.speciesguide.SpeciesGuideScreen
import com.example.nammahasiru.ui.statusupdate.StatusUpdateScreen
import com.example.nammahasiru.ui.treemap.TreeMapScreen

@Composable
fun NammaHasiruNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    
    CompositionLocalProvider(LocalLifecycleOwner provides activity) {
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner, navController) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    activity.intent?.let { intent ->
                        navController.handleDeepLink(intent)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = modifier,
        ) {
            composable(route = Routes.HOME) {
                DashboardScreen(
                    onNewPlant = { navController.navigate(Routes.NEW_PLANT) },
                    onOpenMap = { navController.navigateToTab(Routes.MAP) },
                    onOpenSpecies = { navController.navigateToTab(Routes.SPECIES) },
                    onOpenReminders = { navController.navigate(Routes.REMINDERS) },
                )
            }

            composable(route = Routes.MAP) {
                TreeMapScreen(
                    onUpdateStatus = { saplingId ->
                        navController.navigate(Routes.statusUpdate(saplingId))
                    },
                )
            }

            composable(route = Routes.SPECIES) {
                SpeciesGuideScreen()
            }

            composable(route = Routes.PROFILE) {
                ProfileScreen()
            }

            composable(route = Routes.NEW_PLANT) {
                NewPlantScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(route = Routes.REMINDERS) {
                RemindersScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = "${Routes.STATUS_UPDATE}/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "nammahasiru://status/{id}" },
                ),
            ) { entry ->
                val saplingId = entry.arguments?.getLong("id") ?: 0L
                StatusUpdateScreen(
                    saplingId = saplingId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
