package com.NFC.SecureShield.ui.tunnel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.domain.model.VpnState
import com.NFC.SecureShield.domain.usecase.vpn.ObserveVpnStateUseCase
import com.NFC.SecureShield.domain.usecase.vpn.StartVpnUseCase
import com.NFC.SecureShield.domain.usecase.vpn.StopVpnUseCase
import com.NFC.SecureShield.ui.vpn.VpnEvent
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

data class TunnelUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val isLoading: Boolean = false,
    val dnsFilterEnabled: Boolean = true,
    val trackerBlockEnabled: Boolean = true,
    val killSwitchEnabled: Boolean = false,
    val wifiGuardEnabled: Boolean = true,
    val blockedCount: Int = 0,
)

@HiltViewModel
class TunnelViewModel @Inject constructor(
    observeVpnState: ObserveVpnStateUseCase,
    private val startVpn: StartVpnUseCase,
    private val stopVpn: StopVpnUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TunnelUiState())
    val uiState: StateFlow<TunnelUiState> = _uiState.asStateFlow()

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

    fun onVpnPermissionGranted() { startVpn.start() }
    fun onDisconnectClick() { stopVpn() }

    fun setDnsFilter(v: Boolean) { _uiState.update { it.copy(dnsFilterEnabled = v) } }
    fun setTrackerBlock(v: Boolean) { _uiState.update { it.copy(trackerBlockEnabled = v) } }
    fun setKillSwitch(v: Boolean) { _uiState.update { it.copy(killSwitchEnabled = v) } }
    fun setWifiGuard(v: Boolean) { _uiState.update { it.copy(wifiGuardEnabled = v) } }
}
