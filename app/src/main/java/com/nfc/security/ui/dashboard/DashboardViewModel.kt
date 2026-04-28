package com.nfc.security.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.domain.repository.EventRepository
import com.nfc.security.domain.usecase.freemium.ObserveFreemiumStateUseCase
import com.nfc.security.domain.usecase.nfc.ObserveNfcStateUseCase
import com.nfc.security.domain.usecase.security.ObserveSecurityHealthUseCase
import com.nfc.security.domain.usecase.vpn.ObserveVpnStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeNfcState: ObserveNfcStateUseCase,
    private val observeVpnState: ObserveVpnStateUseCase,
    private val observeSecurityHealth: ObserveSecurityHealthUseCase,
    private val observeFreemiumState: ObserveFreemiumStateUseCase,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val sinceMs = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeNfcState(),
                observeVpnState(),
                observeSecurityHealth(),
                observeFreemiumState()
            ) { nfc, vpn, health, freemium ->
                _uiState.update {
                    it.copy(
                        nfcEnabled = nfc,
                        vpnState = vpn,
                        healthScore = health,
                        freemiumState = freemium,
                        isLoading = false
                    )
                }
            }.collect {

            }
        }
        viewModelScope.launch {
            eventRepository.observeUnreadCount().collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
        viewModelScope.launch {
            eventRepository.observeSince(sinceMs).collect { events ->
                _uiState.update { it.copy(sparklineData = buildSparkline(events)) }
            }
        }
    }

    private fun buildSparkline(events: List<com.nfc.security.data.db.EventEntity>): List<Int> {
        val buckets = IntArray(24)
        val now = System.currentTimeMillis()
        events.forEach { event ->
            val hoursAgo = TimeUnit.MILLISECONDS.toHours(now - event.createdAt).toInt()
            if (hoursAgo in 0..23) buckets[23 - hoursAgo]++
        }
        return buckets.toList()
    }
}
