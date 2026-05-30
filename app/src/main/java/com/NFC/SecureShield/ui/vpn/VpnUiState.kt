package com.NFC.SecureShield.ui.vpn

import com.NFC.SecureShield.domain.model.VpnState

data class VpnUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val isLoading: Boolean = false
)
