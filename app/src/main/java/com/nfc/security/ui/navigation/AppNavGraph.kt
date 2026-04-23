package com.nfc.security.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.ui.dashboard.DashboardScreen
import com.nfc.security.ui.dashboard.DashboardViewModel
import com.nfc.security.ui.freemium.PaywallScreen
import com.nfc.security.ui.freemium.PaywallViewModel
import com.nfc.security.ui.nfc.NfcMonitorScreen
import com.nfc.security.ui.nfc.NfcMonitorViewModel
import com.nfc.security.ui.scan.CleanupScanScreen
import com.nfc.security.ui.scan.ScanViewModel
import com.nfc.security.ui.security.SecurityHealthScreen
import com.nfc.security.ui.security.SecurityHealthViewModel
import com.nfc.security.ui.settings.SettingsScreen
import com.nfc.security.ui.vpn.VpnScreen
import com.nfc.security.ui.vpn.VpnViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.DASHBOARD) {

        composable(NavRoutes.DASHBOARD) {
            val vm: DashboardViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.freemiumState) {
                if (state.freemiumState is FreemiumState.Expired) {
                    navController.navigate(NavRoutes.PAYWALL) {
                        popUpTo(NavRoutes.DASHBOARD) { inclusive = false }
                    }
                }
            }
            DashboardScreen(state = state, onNavigate = { navController.navigate(it) })
        }

        composable(NavRoutes.NFC_MONITOR) {
            val vm: NfcMonitorViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            NfcMonitorScreen(state = state, onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.VPN) {
            val vm: VpnViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            VpnScreen(
                state = state,
                events = vm.events,
                onConnect = vm::onConnectClick,
                onDisconnect = vm::onDisconnectClick,
                onPermissionGranted = vm::onVpnPermissionGranted,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SECURITY_HEALTH) {
            val vm: SecurityHealthViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            SecurityHealthScreen(state = state, onRefresh = vm::refresh, onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.CLEANUP_SCAN) {
            val vm: ScanViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            CleanupScanScreen(
                state = state,
                onStartScan = vm::onStartScan,
                onClearCache = vm::onClearCache,
                onDismissClearedNotice = vm::dismissClearedNotice,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.PAYWALL) {
            val vm: PaywallViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.freemiumState) {
                if (state.freemiumState !is FreemiumState.Expired) {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.PAYWALL) { inclusive = true }
                    }
                }
            }
            PaywallScreen(state = state, onUnlockPremium = vm::onUnlockPremium)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
