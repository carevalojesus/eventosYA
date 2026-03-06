package com.carevalojesus.eventosya.ui.screens.admin

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class AdminRoute(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Events : AdminRoute("admin_events", "Eventos", Icons.Filled.Event, Icons.Outlined.Event)
    data object Tickets : AdminRoute("admin_tickets", "Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber)
    data object Profile : AdminRoute("admin_profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

val adminBottomTabs = listOf(AdminRoute.Events, AdminRoute.Tickets, AdminRoute.Profile)

@Composable
fun AdminDashboard(
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToEditEvent: (String) -> Unit,
    onLogout: () -> Unit,
    dynamicColorEnabled: Boolean,
    highContrastEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    adminBottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
                AdminNavHostContent(
                    navController = navController,
                    modifier = Modifier.weight(1f),
                    onNavigateToCreateEvent = onNavigateToCreateEvent,
                    onNavigateToEditEvent = onNavigateToEditEvent,
                    onLogout = onLogout,
                    dynamicColorEnabled = dynamicColorEnabled,
                    highContrastEnabled = highContrastEnabled,
                    onDynamicColorChange = onDynamicColorChange,
                    onHighContrastChange = onHighContrastChange
                )
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        adminBottomTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                AdminNavHostContent(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToCreateEvent = onNavigateToCreateEvent,
                    onNavigateToEditEvent = onNavigateToEditEvent,
                    onLogout = onLogout,
                    dynamicColorEnabled = dynamicColorEnabled,
                    highContrastEnabled = highContrastEnabled,
                    onDynamicColorChange = onDynamicColorChange,
                    onHighContrastChange = onHighContrastChange
                )
            }
        }
    }
}

@Composable
private fun AdminNavHostContent(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToEditEvent: (String) -> Unit,
    onLogout: () -> Unit,
    dynamicColorEnabled: Boolean,
    highContrastEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AdminRoute.Events.route,
        modifier = modifier
    ) {
        composable(AdminRoute.Events.route) {
            val adminHomeViewModel: AdminHomeViewModel = viewModel()
            AdminHomeScreen(
                viewModel = adminHomeViewModel,
                onCreateEvent = onNavigateToCreateEvent,
                onEditEvent = onNavigateToEditEvent,
                onLogout = onLogout
            )
        }

        composable(AdminRoute.Tickets.route) {
            val ticketsViewModel: AdminTicketsViewModel = viewModel()
            AdminTicketsScreen(viewModel = ticketsViewModel)
        }

        composable(AdminRoute.Profile.route) {
            AdminProfileScreen(
                onLogout = onLogout,
                dynamicColorEnabled = dynamicColorEnabled,
                highContrastEnabled = highContrastEnabled,
                onDynamicColorChange = onDynamicColorChange,
                onHighContrastChange = onHighContrastChange
            )
        }
    }
}
