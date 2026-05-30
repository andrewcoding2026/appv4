package com.NFC.SecureShield.free.ui.vpn

import com.NFC.SecureShield.free.domain.model.VpnState

data class VpnUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val isLoading: Boolean = false
)
