package com.NFC.SecureShield.ui.vault

import com.NFC.SecureShield.data.db.VaultItemEntity

sealed class VaultUiState {
    object Locked : VaultUiState()
    object Unlocking : VaultUiState()
    data class Unlocked(val items: List<VaultItemEntity>) : VaultUiState()
    data class Error(val message: String) : VaultUiState()
}
