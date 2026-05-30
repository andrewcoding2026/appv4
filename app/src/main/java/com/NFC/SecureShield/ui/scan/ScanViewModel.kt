package com.NFC.SecureShield.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.domain.usecase.scan.ClearAppCacheUseCase
import com.NFC.SecureShield.domain.usecase.scan.ScanForMalwareUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanForMalware: ScanForMalwareUseCase,
    private val clearAppCache: ClearAppCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onStartScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null, clearedBytes = null) }
            try {
                val report = scanForMalware()
                _uiState.update { it.copy(isScanning = false, report = report) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = e.message ?: "Scan failed") }
            }
        }
    }

    fun onClearCache() {
        viewModelScope.launch {
            try {
                val bytes = clearAppCache()
                _uiState.update { it.copy(clearedBytes = bytes) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to clear cache") }
            }
        }
    }

    fun dismissClearedNotice() {
        _uiState.update { it.copy(clearedBytes = null) }
    }
}
