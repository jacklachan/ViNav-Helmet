package com.vinav.helmet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vinav.helmet.ui.screens.activenav.ActiveNavigationScreen
import com.vinav.helmet.ui.screens.connect.ConnectHelmetScreen
import com.vinav.helmet.ui.screens.debug.DebugScreen
import com.vinav.helmet.ui.screens.home.HomeScreen
import com.vinav.helmet.ui.screens.route.RouteScreen
import com.vinav.helmet.ui.screens.savedplaces.SavedPlacesScreen
import com.vinav.helmet.ui.screens.settings.SettingsScreen
import com.vinav.helmet.ui.screens.sos.SOSScreen
import com.vinav.helmet.ui.screens.splash.SplashScreen

@Composable
fun ViNavNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToConnect = { navController.navigate(Screen.ConnectHelmet.route) },
                onNavigateToRoute = { navController.navigate(Screen.Route.route) },
                onNavigateToActiveNav = { navController.navigate(Screen.ActiveNavigation.route) },
                onNavigateToSavedPlaces = { navController.navigate(Screen.SavedPlaces.route) },
                onNavigateToSOS = { navController.navigate(Screen.SOS.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDebug = { navController.navigate(Screen.Debug.route) }
            )
        }
        composable(Screen.ConnectHelmet.route) {
            ConnectHelmetScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Route.route) {
            RouteScreen(
                onBack = { navController.popBackStack() },
                onNavigateToActiveNav = {
                    navController.navigate(Screen.ActiveNavigation.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.ActiveNavigation.route) {
            ActiveNavigationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SavedPlaces.route) {
            SavedPlacesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SOS.route) {
            SOSScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Debug.route) {
            DebugScreen(onBack = { navController.popBackStack() })
        }
    }
}
