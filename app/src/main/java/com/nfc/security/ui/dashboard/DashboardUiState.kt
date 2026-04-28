package com.nfc.security.ui.dashboard

import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.domain.model.SecurityHealthScore
import com.nfc.security.domain.model.VpnState

data class DashboardUiState(
    val nfcEnabled: Boolean = false,
    val vpnState: VpnState = VpnState.Disconnected,
    val healthScore: SecurityHealthScore? = null,
    val freemiumState: FreemiumState = FreemiumState.Trial(7 * 24 * 60 * 60 * 1000L),
    val isLoading: Boolean = true,
    val unreadCount: Int = 0,
    val sparklineData: List<Int> = emptyList(),
)
