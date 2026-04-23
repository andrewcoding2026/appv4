package com.nfc.security.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.domain.usecase.freemium.ObserveFreemiumStateUseCase
import com.nfc.security.domain.usecase.nfc.ObserveNfcStateUseCase
import com.nfc.security.domain.usecase.security.ObserveSecurityHealthUseCase
import com.nfc.security.domain.usecase.vpn.ObserveVpnStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeNfcState: ObserveNfcStateUseCase,
    observeVpnState: ObserveVpnStateUseCase,
    observeSecurityHealth: ObserveSecurityHealthUseCase,
    observeFreemiumState: ObserveFreemiumStateUseCase
) : ViewModel() {

    val uiState = combine(
        observeNfcState(),
        observeVpnState(),
        observeSecurityHealth(),
        observeFreemiumState()
    ) { nfcEnabled, vpnState, healthScore, freemiumState ->
        DashboardUiState(
            nfcEnabled = nfcEnabled,
            vpnState = vpnState,
            healthScore = healthScore,
            freemiumState = freemiumState,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
