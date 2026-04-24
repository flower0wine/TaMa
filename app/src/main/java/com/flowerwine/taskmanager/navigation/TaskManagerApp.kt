package com.flowerwine.taskmanager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flowerwine.taskmanager.core.di.AppContainer
import com.flowerwine.taskmanager.core.di.AppViewModelFactory
import com.flowerwine.taskmanager.feature.analysis.AnalysisScreen
import com.flowerwine.taskmanager.feature.analysis.AnalysisViewModel
import com.flowerwine.taskmanager.feature.apps.AppsScreen
import com.flowerwine.taskmanager.feature.apps.AppsViewModel
import com.flowerwine.taskmanager.feature.overview.OverviewScreen
import com.flowerwine.taskmanager.feature.overview.OverviewViewModel
import com.flowerwine.taskmanager.feature.tools.ToolsScreen
import com.flowerwine.taskmanager.feature.tools.ToolsViewModel

@Composable
fun TaskManagerApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(appContainer)
    val overviewViewModel: OverviewViewModel = viewModel(factory = factory)
    val analysisViewModel: AnalysisViewModel = viewModel(factory = factory)
    val appsViewModel: AppsViewModel = viewModel(factory = factory)
    val toolsViewModel: ToolsViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val destination = navBackStackEntry?.destination
            NavigationBar {
                TopLevelDestination.entries.forEach { screen ->
                    val selected = destination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Overview.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Overview.route) {
                val state by overviewViewModel.uiState.collectAsStateWithLifecycle()
                OverviewScreen(state = state)
            }
            composable(TopLevelDestination.Analysis.route) {
                val state by analysisViewModel.uiState.collectAsStateWithLifecycle()
                AnalysisScreen(
                    state = state,
                    onRangeSelected = analysisViewModel::onRangeSelected,
                )
            }
            composable(TopLevelDestination.Apps.route) {
                val state by appsViewModel.uiState.collectAsStateWithLifecycle()
                AppsScreen(
                    state = state,
                    onQueryChanged = appsViewModel::onQueryChanged,
                    onSortSelected = appsViewModel::onSortSelected,
                    onOpenUsageAccess = { appContainer.settingsNavigator.open(com.flowerwine.taskmanager.core.model.ToolActionType.OpenUsageAccess) },
                    onOpenAppDetails = { packageName ->
                        appContainer.settingsNavigator.open(
                            com.flowerwine.taskmanager.core.model.ToolActionType.OpenAppDetails,
                            packageName,
                        )
                    },
                )
            }
            composable(TopLevelDestination.Tools.route) {
                val state by toolsViewModel.uiState.collectAsStateWithLifecycle()
                ToolsScreen(
                    state = state,
                    onAction = { action ->
                        appContainer.settingsNavigator.open(action)
                    },
                )
            }
        }
    }
}
