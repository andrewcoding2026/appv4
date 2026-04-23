package com.nfc.security.ui.security

import com.nfc.security.domain.model.SecurityHealthScore

data class SecurityHealthUiState(
    val score: SecurityHealthScore? = null,
    val isLoading: Boolean = true
)
