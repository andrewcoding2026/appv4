package com.nfc.security.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.ui.components.AegisBottomTabs
import com.nfc.security.ui.dashboard.DashboardScreen
import com.nfc.security.ui.dashboard.DashboardViewModel
import com.nfc.security.ui.freemium.PaywallScreen
import com.nfc.security.ui.freemium.PaywallViewModel
import com.nfc.security.ui.incident.IncidentDetailScreen
import com.nfc.security.ui.incident.IncidentDetailViewModel
import com.nfc.security.ui.integrity.IntegrityScreen
import com.nfc.security.ui.integrity.IntegrityViewModel
import com.nfc.security.ui.nfc.NfcSentinelScreen
import com.nfc.security.ui.nfc.NfcMonitorViewModel
import com.nfc.security.ui.notifications.NotificationsScreen
import com.nfc.security.ui.notifications.NotificationsViewModel
import com.nfc.security.ui.onboarding.OnboardingScreen
import com.nfc.security.ui.onboarding.OnboardingViewModel
import com.nfc.security.ui.scan.ScanScreen
import com.nfc.security.ui.scan.ScanViewModel
import com.nfc.security.ui.settings.SettingsScreen
import com.nfc.security.ui.settings.SettingsViewModel
import com.nfc.security.ui.tunnel.TunnelScreen
import com.nfc.security.ui.tunnel.TunnelViewModel
import com.nfc.security.ui.vault.VaultScreen
import com.nfc.security.ui.vault.VaultViewModel

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in NavRoutes.bottomTabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AegisBottomTabs(
                    activeRoute = currentRoute ?: NavRoutes.DASHBOARD,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.ONBOARDING) {
                val vm: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = vm,
                    onFinish = {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.DASHBOARD) {
                val vm: DashboardViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                androidx.compose.runtime.LaunchedEffect(state.freemiumState) {
                    if (state.freemiumState is FreemiumState.Expired) {
                        navController.navigate(NavRoutes.PAYWALL) {
                            popUpTo(NavRoutes.DASHBOARD) { inclusive = false }
                        }
                    }
                }
                DashboardScreen(
                    state = state,
                    onNavigate = { navController.navigate(it) }
                )
            }

            composable(NavRoutes.NOTIFICATIONS) {
                val vm: NotificationsViewModel = hiltViewModel()
                NotificationsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEventClick = { eventId ->
                        navController.navigate(NavRoutes.incidentDetail(eventId))
                    }
                )
            }

            composable(NavRoutes.NFC_SENTINEL) {
                val vm: NfcMonitorViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                NfcSentinelScreen(
                    state = state,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.TUNNEL) {
                val vm: TunnelViewModel = hiltViewModel()
                TunnelScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.SCAN) {
                val vm: ScanViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                ScanScreen(
                    state = state,
                    onStartScan = vm::onStartScan,
                    onClearCache = vm::onClearCache,
                    onDismissClearedNotice = vm::dismissClearedNotice,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.VAULT) {
                val vm: VaultViewModel = hiltViewModel()
                VaultScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.INTEGRITY) {
                val vm: IntegrityViewModel = hiltViewModel()
                IntegrityScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.INCIDENT_DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.LongType })
            ) { backStack ->
                val eventId = backStack.arguments?.getLong("eventId") ?: return@composable
                val vm: IncidentDetailViewModel = hiltViewModel()
                IncidentDetailScreen(
                    eventId = eventId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PAYWALL) {
                val vm: PaywallViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                androidx.compose.runtime.LaunchedEffect(state.freemiumState) {
                    if (state.freemiumState !is FreemiumState.Expired) {
                        navController.navigate(NavRoutes.DASHBOARD) {
                            popUpTo(NavRoutes.PAYWALL) { inclusive = true }
                        }
                    }
                }
                PaywallScreen(state = state, onUnlockPremium = vm::onUnlockPremium)
            }

            composable(NavRoutes.SETTINGS) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNavigateToIntegrity = { navController.navigate(NavRoutes.INTEGRITY) }
                )
            }
        }
    }
}
