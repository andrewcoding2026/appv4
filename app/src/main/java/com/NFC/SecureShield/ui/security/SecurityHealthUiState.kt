package com.NFC.SecureShield.ui.security

import com.NFC.SecureShield.domain.model.SecurityHealthScore

data class SecurityHealthUiState(
    val score: SecurityHealthScore? = null,
    val isLoading: Boolean = true
)
