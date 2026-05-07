package com.nfcsecurity.ui.integrity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcsecurity.domain.model.SecurityHealthScore
import com.nfcsecurity.domain.usecase.security.ObserveSecurityHealthUseCase
import com.nfcsecurity.domain.usecase.security.RunSecurityChecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntegrityUiState(
    val score: SecurityHealthScore? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class IntegrityViewModel @Inject constructor(
    observeSecurityHealth: ObserveSecurityHealthUseCase,
    private val runChecks: RunSecurityChecksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntegrityUiState())
    val uiState: StateFlow<IntegrityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSecurityHealth().collect { score ->
                _uiState.update { it.copy(score = score, isLoading = false) }
            }
        }
    }

    fun auditNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val score = runChecks()
            _uiState.update { it.copy(score = score, isLoading = false) }
        }
    }
}
