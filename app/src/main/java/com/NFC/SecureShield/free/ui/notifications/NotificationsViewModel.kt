package com.NFC.SecureShield.free.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NFC.SecureShield.free.data.db.EventEntity
import com.NFC.SecureShield.free.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotifFilter { ALL, UNREAD, INCIDENTS }

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(NotifFilter.ALL)
    val filter: StateFlow<NotifFilter> = _filter.asStateFlow()

    val events: StateFlow<List<EventEntity>> = combine(
        eventRepository.observeAll(),
        _filter
    ) { all, f ->
        when (f) {
            NotifFilter.ALL -> all
            NotifFilter.UNREAD -> all.filter { !it.read }
            NotifFilter.INCIDENTS -> all.filter { it.severity == "crit" || it.severity == "warn" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(f: NotifFilter) { _filter.value = f }

    fun markAllRead() {
        viewModelScope.launch { eventRepository.markAllRead() }
    }
}
