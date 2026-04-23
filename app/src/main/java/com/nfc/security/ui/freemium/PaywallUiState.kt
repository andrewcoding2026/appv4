package com.nfc.security.ui.freemium

import com.nfc.security.domain.model.FreemiumState

data class PaywallUiState(
    val freemiumState: FreemiumState = FreemiumState.Expired,
    val isUnlocking: Boolean = false
)
