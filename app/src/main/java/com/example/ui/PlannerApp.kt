package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.ThemePreference
import com.example.ui.components.CurrencyPillButton
import com.example.ui.components.CurrencySelectorDialog
import com.example.ui.components.SaveScenarioDialog
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MethodologyScreen
import com.example.ui.screens.ScenariosScreen
import com.example.ui.state.PlannerViewModel
import com.example.ui.theme.FacelessPlannerTheme

object PlannerRoutes {
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_CALCULATOR = "calculator"
    const val ROUTE_SCENARIOS = "scenarios"
    const val ROUTE_METHODOLOGY = "methodology"
}

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelDestinations = listOf(
    NavDestinationItem(
        route = PlannerRoutes.ROUTE_DASHBOARD,
        label = "Resumen",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    NavDestinationItem(
        route = PlannerRoutes.ROUTE_CALCULATOR,
        label = "Calculadora",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        testTag = "nav_calculator"
    ),
    NavDestinationItem(
        route = PlannerRoutes.ROUTE_SCENARIOS,
        label = "Escenarios",
        selectedIcon = Icons.Filled.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        testTag = "nav_scenarios"
    ),
    NavDestinationItem(
        route = PlannerRoutes.ROUTE_METHODOLOGY,
        label = "Metodología",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "nav_methodology"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerApp(viewModel: PlannerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (uiState.themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    FacelessPlannerTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: PlannerRoutes.ROUTE_DASHBOARD

        val snackbarHostState = remember { SnackbarHostState() }
        var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
        var showSaveDialog by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(uiState.snackbarMessage) {
            uiState.snackbarMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.consumeSnackbarMessage()
            }
        }

        if (showCurrencyDialog) {
            CurrencySelectorDialog(
                selectedCurrency = uiState.selectedCurrency,
                onSelectCurrency = viewModel::selectCurrency,
                onDismiss = { showCurrencyDialog = false }
            )
        }

        if (showSaveDialog) {
            SaveScenarioDialog(
                initialName = "",
                initialNotes = "",
                title = "Guardar escenario",
                confirmLabel = "Guardar",
                onConfirm = { name, notes ->
                    viewModel.saveAsNewScenario(name, notes)
                },
                onDismiss = { showSaveDialog = false }
            )
        }

        val navigateToTab: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val currentDestinationTitle = when (currentRoute) {
            PlannerRoutes.ROUTE_CALCULATOR -> "Calculadora de ingresos"
            PlannerRoutes.ROUTE_SCENARIOS -> "Escenarios y comparación"
            PlannerRoutes.ROUTE_METHODOLOGY -> "Metodología y guía"
            else -> "Faceless Revenue Planner"
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp
            val isWideDashboard = maxWidth >= 840.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentDestinationTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Solo estimaciones locales",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            CurrencyPillButton(
                                currency = uiState.selectedCurrency,
                                onClick = { showCurrencyDialog = true }
                            )
                            IconButton(
                                onClick = viewModel::cycleThemePreference,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("theme_toggle_button")
                            ) {
                                val (icon, desc) = when (uiState.themePreference) {
                                    ThemePreference.SYSTEM ->
                                        Icons.Outlined.SettingsBrightness to "Tema automático del sistema"
                                    ThemePreference.LIGHT ->
                                        Icons.Outlined.LightMode to "Tema claro activo"
                                    ThemePreference.DARK ->
                                        Icons.Outlined.DarkMode to "Tema oscuro activo"
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = desc
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar {
                            topLevelDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            topLevelDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        NavHost(
                            navController = navController,
                            startDestination = PlannerRoutes.ROUTE_DASHBOARD
                        ) {
                            composable(PlannerRoutes.ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    uiState = uiState,
                                    isWideScreen = isWideDashboard,
                                    onNavigateToCalculator = {
                                        navigateToTab(PlannerRoutes.ROUTE_CALCULATOR)
                                    },
                                    onOpenSaveScenario = { showSaveDialog = true },
                                    onLoadSampleData = viewModel::loadSampleIntoCalculator
                                )
                            }
                            composable(PlannerRoutes.ROUTE_CALCULATOR) {
                                CalculatorScreen(
                                    uiState = uiState,
                                    onFieldChanged = viewModel::onFieldChanged,
                                    onResetConfirmed = viewModel::resetCalculator,
                                    onUpdateActiveScenario = viewModel::updateActiveScenario,
                                    onOpenSaveAsNew = { showSaveDialog = true }
                                )
                            }
                            composable(PlannerRoutes.ROUTE_SCENARIOS) {
                                ScenariosScreen(
                                    uiState = uiState,
                                    onLoadScenario = { scenario ->
                                        viewModel.loadScenario(scenario)
                                        navigateToTab(PlannerRoutes.ROUTE_DASHBOARD)
                                    },
                                    onEditScenarioValues = { scenario ->
                                        viewModel.loadScenario(scenario)
                                        navigateToTab(PlannerRoutes.ROUTE_CALCULATOR)
                                    },
                                    onRenameScenario = viewModel::renameAndUpdateNotes,
                                    onDuplicateScenario = viewModel::duplicateScenario,
                                    onDeleteScenario = viewModel::deleteScenario,
                                    onToggleCompareScenario = viewModel::toggleCompareSelection,
                                    onOpenSaveNewScenario = { showSaveDialog = true },
                                    onLoadSampleScenario = {
                                        viewModel.loadSampleIntoCalculator()
                                        navigateToTab(PlannerRoutes.ROUTE_DASHBOARD)
                                    }
                                )
                            }
                            composable(PlannerRoutes.ROUTE_METHODOLOGY) {
                                MethodologyScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
