package com.NFC.SecureShield.ui.vpn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.domain.usecase.vpn.ObserveVpnStateUseCase
import com.NFC.SecureShield.domain.usecase.vpn.StartVpnUseCase
import com.NFC.SecureShield.domain.usecase.vpn.StopVpnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
    observeVpnState: ObserveVpnStateUseCase,
    private val startVpn: StartVpnUseCase,
    private val stopVpn: StopVpnUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VpnEvent>()
    val events: SharedFlow<VpnEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeVpnState().collect { vpnState ->
                _uiState.update { it.copy(vpnState = vpnState, isLoading = false) }
            }
        }
    }

    fun onConnectClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val prepareIntent = startVpn.prepareIntent()
            if (prepareIntent != null) {
                _events.emit(VpnEvent.RequestVpnPermission(prepareIntent))
                _uiState.update { it.copy(isLoading = false) }
            } else {
                startVpn.start()
            }
        }
    }

    fun onVpnPermissionGranted() {
        startVpn.start()
    }

    fun onDisconnectClick() {
        stopVpn()
    }
}
