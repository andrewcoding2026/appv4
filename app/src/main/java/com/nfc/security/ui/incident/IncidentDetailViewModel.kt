package com.nfc.security.ui.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.data.db.EventEntity
import com.nfc.security.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncidentDetailUiState(
    val event: EventEntity? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
)

@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentDetailUiState())
    val uiState: StateFlow<IncidentDetailUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            val event = eventRepository.getById(id)
            _uiState.value = IncidentDetailUiState(event = event, isLoading = false)
        }
    }

    fun deleteEvent() {
        val id = _uiState.value.event?.id ?: return
        viewModelScope.launch {
            eventRepository.deleteById(id)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
