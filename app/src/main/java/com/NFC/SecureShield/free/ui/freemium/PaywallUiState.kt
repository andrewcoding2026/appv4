package com.NFC.SecureShield.free.ui.freemium

import com.NFC.SecureShield.free.domain.model.FreemiumState

data class PaywallUiState(
    val freemiumState: FreemiumState = FreemiumState.Expired,
    val isUnlocking: Boolean = false
)
