package com.vinav.helmet.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object ConnectHelmet : Screen("connect_helmet")
    data object Route : Screen("route")
    data object ActiveNavigation : Screen("active_navigation")
    data object SavedPlaces : Screen("saved_places")
    data object SOS : Screen("sos")
    data object Settings : Screen("settings")
    data object Debug : Screen("debug")
}
