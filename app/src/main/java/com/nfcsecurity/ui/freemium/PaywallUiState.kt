package com.nfcsecurity.ui.freemium

import com.nfcsecurity.domain.model.FreemiumState

data class PaywallUiState(
    val freemiumState: FreemiumState = FreemiumState.Expired,
    val isUnlocking: Boolean = false
)
