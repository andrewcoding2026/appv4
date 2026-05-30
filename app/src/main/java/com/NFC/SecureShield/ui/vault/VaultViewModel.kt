package com.NFC.SecureShield.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.data.crypto.AesGcm
import com.NFC.SecureShield.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val aesGcm: AesGcm,
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

    /** Returns an AES/GCM Cipher ready to wrap in [BiometricPrompt.CryptoObject] for an encrypt operation. */
    fun prepareEncryptCipher(): Cipher? = runCatching { aesGcm.prepareEncryptCipher() }.getOrNull()

    /** Returns an AES/GCM Cipher (seeded with [iv]) ready to wrap in [BiometricPrompt.CryptoObject] for a decrypt operation. */
    fun prepareDecryptCipher(iv: ByteArray): Cipher? = runCatching { aesGcm.prepareDecryptCipher(iv) }.getOrNull()

    fun addItem(label: String, type: String, secret: String, encryptCipher: Cipher) {
        viewModelScope.launch {
            vaultRepository.addItem(label, type, secret.toByteArray(), encryptCipher)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            vaultRepository.deleteItem(id)
        }
    }

    fun decryptItem(id: Long, decryptCipher: Cipher, onResult: (ByteArray?) -> Unit) {
        viewModelScope.launch {
            onResult(vaultRepository.decryptItem(id, decryptCipher))
        }
    }
}