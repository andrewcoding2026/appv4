package com.nfcsecurity.ui.security

import com.nfcsecurity.domain.model.SecurityHealthScore

data class SecurityHealthUiState(
    val score: SecurityHealthScore? = null,
    val isLoading: Boolean = true
)
