package com.NFC.SecureShield.free.ui.security

import com.NFC.SecureShield.free.domain.model.SecurityHealthScore

data class SecurityHealthUiState(
    val score: SecurityHealthScore? = null,
    val isLoading: Boolean = true
)
