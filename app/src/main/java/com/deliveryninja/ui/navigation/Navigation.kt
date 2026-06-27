package com.deliveryninja.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.dashboard.DashboardScreen
import com.deliveryninja.ui.earnings.EarningsScreen
import com.deliveryninja.ui.goals.GoalsScreen
import com.deliveryninja.ui.heatmap.HeatmapScreen
import com.deliveryninja.ui.assistant.AssistantScreen
import com.deliveryninja.ui.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Earnings : Screen("earnings", "Earnings", Icons.Default.AttachMoney)
    object Heatmap : Screen("heatmap", "Map", Icons.Default.Map)
    object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    object Assistant : Screen("assistant", "Mitra AI", Icons.Default.Psychology)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard, Screen.Earnings, Screen.Heatmap,
    Screen.Goals, Screen.Assistant, Screen.Settings
)

@Composable
fun DeliveryNinjaNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel, padding) }
            composable(Screen.Earnings.route) { EarningsScreen(viewModel, padding) }
            composable(Screen.Heatmap.route) { HeatmapScreen(viewModel, padding) }
            composable(Screen.Goals.route) { GoalsScreen(viewModel, padding) }
            composable(Screen.Assistant.route) { AssistantScreen(viewModel, padding) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel, padding) }
        }
    }
}
