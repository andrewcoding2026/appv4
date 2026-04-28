package com.nfc.security.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Locked)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    fun onUnlocked() {
        _uiState.value = VaultUiState.Unlocking
        viewModelScope.launch {
            vaultRepository.observeAll().collect { items ->
                _uiState.value = VaultUiState.Unlocked(items)
            }
        }
    }

    fun lock() {
        _uiState.value = VaultUiState.Locked
    }

    fun addItem(label: String, type: String, secret: String) {
        viewModelScope.launch {
            vaultRepository.addItem(label, type, secret.toByteArray())
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            vaultRepository.deleteItem(id)
        }
    }
}