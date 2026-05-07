package com.nfcsecurity.ui.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcsecurity.domain.model.NfcTagInfo
import com.nfcsecurity.domain.usecase.nfc.ObserveNfcStateUseCase
import com.nfcsecurity.domain.repository.NfcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NfcMonitorViewModel @Inject constructor(
    private val observeNfcState: ObserveNfcStateUseCase,
    private val nfcRepository: NfcRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NfcMonitorUiState())
    val uiState: StateFlow<NfcMonitorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeNfcState().collect { enabled ->
                _uiState.update { it.copy(isNfcEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            nfcRepository.getLastDiscoveredTag().collect { tag ->
                tag ?: return@collect
                _uiState.update { state ->
                    state.copy(
                        lastTag = tag,
                        tagHistory = (listOf(tag) + state.tagHistory).take(20)
                    )
                }
            }
        }
    }

    fun onNewTagDiscovered(tag: NfcTagInfo) {
        viewModelScope.launch {
            nfcRepository.publishTag(tag)
        }
    }
}
