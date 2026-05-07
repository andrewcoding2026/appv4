package com.nfcsecurity.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcsecurity.domain.usecase.security.ObserveSecurityHealthUseCase
import com.nfcsecurity.domain.usecase.security.RunSecurityChecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityHealthViewModel @Inject constructor(
    observeSecurityHealth: ObserveSecurityHealthUseCase,
    private val runSecurityChecks: RunSecurityChecksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityHealthUiState())
    val uiState: StateFlow<SecurityHealthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSecurityHealth().collect { score ->
                _uiState.update { it.copy(score = score, isLoading = false) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val score = runSecurityChecks()
            _uiState.update { it.copy(score = score, isLoading = false) }
        }
    }
}
