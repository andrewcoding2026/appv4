package com.NFC.SecureShield.ui.freemium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.domain.usecase.freemium.ObserveFreemiumStateUseCase
import com.NFC.SecureShield.domain.usecase.freemium.UnlockPremiumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    observeFreemiumState: ObserveFreemiumStateUseCase,
    private val unlockPremium: UnlockPremiumUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeFreemiumState().collect { state ->
                _uiState.update { it.copy(freemiumState = state) }
            }
        }
    }

    fun onUnlockPremium() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUnlocking = true) }
            unlockPremium()
            _uiState.update { it.copy(isUnlocking = false) }
        }
    }
}
