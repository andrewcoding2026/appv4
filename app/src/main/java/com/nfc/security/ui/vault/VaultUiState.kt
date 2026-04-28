package com.nfc.security.ui.vault

import com.nfc.security.data.db.VaultItemEntity

sealed class VaultUiState {
    object Locked : VaultUiState()
    object Unlocking : VaultUiState()
    data class Unlocked(val items: List<VaultItemEntity>) : VaultUiState()
    data class Error(val message: String) : VaultUiState()
}
