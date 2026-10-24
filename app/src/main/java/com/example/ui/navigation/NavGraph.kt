package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.screens.ActivityDiagnosticsScreen
import com.example.ui.screens.FloatingControlsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MacroEditorScreen
import com.example.ui.screens.MacroPreviewScreen
import com.example.ui.screens.MacroRecordingWizard
import com.example.ui.screens.MyMacrosScreen
import com.example.ui.screens.SettingsScreen

sealed class BottomNavItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : BottomNavItem(Screen.Home, "Home", Icons.Default.Home)
    object Macros : BottomNavItem(Screen.MyMacros, "My Macros", Icons.Default.List)
    object Floating : BottomNavItem(Screen.FloatingControls, "Floating Bar", Icons.Default.Layers)
    object Diagnostics : BottomNavItem(Screen.ActivityDiagnostics, "Activity", Icons.Default.Analytics)
    object Settings : BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
}

@Composable
fun MacroFlowNavGraph(navController: NavHostController) {
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Macros,
        BottomNavItem.Floating,
        BottomNavItem.Diagnostics,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on secondary full-screen screens (Wizard, Editor, Preview)
    val showBottomBar = currentRoute in navItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF181B22),
                    contentColor = Color(0xFF22C55E),
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(text = item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFF22C55E),
                                indicatorColor = Color(0xFF22C55E),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCreateMacro = { navController.navigate(Screen.RecordingWizard.createRoute("New Macro")) },
                    onOpenMacros = { navController.navigate(Screen.MyMacros.route) },
                    onEditMacro = { macroId -> navController.navigate(Screen.MacroEditor.createRoute(macroId)) },
                    onOpenFloatingControls = { navController.navigate(Screen.FloatingControls.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.MyMacros.route) {
                MyMacrosScreen(
                    onEditMacro = { macroId -> navController.navigate(Screen.MacroEditor.createRoute(macroId)) },
                    onCreateMacro = { navController.navigate(Screen.RecordingWizard.createRoute("New Macro")) }
                )
            }

            composable(Screen.FloatingControls.route) {
                FloatingControlsScreen()
            }

            composable(Screen.ActivityDiagnostics.route) {
                ActivityDiagnosticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.MacroEditor.route,
                arguments = listOf(navArgument("macroId") { type = NavType.StringType })
            ) { backStackEntry ->
                val macroId = backStackEntry.arguments?.getString("macroId") ?: ""
                MacroEditorScreen(
                    macroId = macroId,
                    onBack = { navController.popBackStack() },
                    onRecordAgain = { name -> navController.navigate(Screen.RecordingWizard.createRoute(name)) },
                    onPreview = { id -> navController.navigate(Screen.MacroPreview.createRoute(id)) }
                )
            }

            composable(
                route = Screen.MacroPreview.route,
                arguments = listOf(navArgument("macroId") { type = NavType.StringType })
            ) { backStackEntry ->
                val macroId = backStackEntry.arguments?.getString("macroId") ?: ""
                MacroPreviewScreen(
                    macroId = macroId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.RecordingWizard.route,
                arguments = listOf(navArgument("macroName") {
                    type = NavType.StringType
                    defaultValue = "New Macro"
                })
            ) { backStackEntry ->
                val macroName = backStackEntry.arguments?.getString("macroName") ?: "New Macro"
                MacroRecordingWizard(
                    initialMacroName = macroName,
                    onBack = { navController.popBackStack() },
                    onMacroSaved = { savedId ->
                        navController.navigate(Screen.MacroEditor.createRoute(savedId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
        }
    }
}
