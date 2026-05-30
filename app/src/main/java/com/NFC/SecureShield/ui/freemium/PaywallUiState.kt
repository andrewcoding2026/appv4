package com.NFC.SecureShield.ui.freemium

import com.NFC.SecureShield.domain.model.FreemiumState

data class PaywallUiState(
    val freemiumState: FreemiumState = FreemiumState.Expired,
    val isUnlocking: Boolean = false
)
