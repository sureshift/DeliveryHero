package com.deliveryninja.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.assistant.AssistantScreen
import com.deliveryninja.ui.dashboard.DashboardScreen
import com.deliveryninja.ui.earnings.EarningsScreen
import com.deliveryninja.ui.goals.GoalsScreen
import com.deliveryninja.ui.heatmap.HeatmapScreen
import com.deliveryninja.ui.settings.SettingsScreen
import com.deliveryninja.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home",     Icons.Default.Home)
    object Earnings  : Screen("earnings",  "Earnings", Icons.Default.AttachMoney)
    object Heatmap   : Screen("heatmap",   "Map",      Icons.Default.Map)
    object Goals     : Screen("goals",     "Goals",    Icons.Default.Flag)
    object Assistant : Screen("assistant", "AI",       Icons.Default.Psychology)
    object Settings  : Screen("settings",  "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard, Screen.Earnings, Screen.Heatmap,
    Screen.Goals, Screen.Assistant, Screen.Settings
)

@Composable
fun DeliveryNinjaNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = NinjaDark,
        bottomBar = {
            NavigationBar(
                containerColor = NinjaCard,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = navBackStackEntry?.destination
                        ?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(screen.label, fontSize = 10.sp) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NinjaOrange,
                            selectedTextColor   = NinjaOrange,
                            unselectedIconColor = NinjaGray,
                            unselectedTextColor = NinjaGray,
                            indicatorColor      = NinjaOrange.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController   = navController,
            startDestination= Screen.Dashboard.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel, padding) }
            composable(Screen.Earnings.route)  { EarningsScreen(viewModel, padding) }
            composable(Screen.Heatmap.route)   { HeatmapScreen(viewModel, padding) }
            composable(Screen.Goals.route)     { GoalsScreen(viewModel, padding) }
            composable(Screen.Assistant.route) { AssistantScreen(viewModel, padding) }
            composable(Screen.Settings.route)  { SettingsScreen(viewModel, padding) }
        }
    }
}
