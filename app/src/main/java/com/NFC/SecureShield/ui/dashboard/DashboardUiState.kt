package com.NFC.SecureShield.ui.dashboard

import com.NFC.SecureShield.domain.model.FreemiumState
import com.NFC.SecureShield.domain.model.SecurityHealthScore
import com.NFC.SecureShield.domain.model.VpnState

data class DashboardUiState(
    val nfcEnabled: Boolean = false,
    val vpnState: VpnState = VpnState.Disconnected,
    val healthScore: SecurityHealthScore? = null,
    val freemiumState: FreemiumState = FreemiumState.Trial(7 * 24 * 60 * 60 * 1000L),
    val isLoading: Boolean = true,
    val unreadCount: Int = 0,
    val sparklineData: List<Int> = emptyList(),
)
