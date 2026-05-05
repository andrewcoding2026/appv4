package com.nfc.security.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.data.datastore.NFCSecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: NFCSecurityPreferences
) : ViewModel() {

    private val _page = MutableStateFlow(0)
    val page: StateFlow<Int> = _page.asStateFlow()

    val totalPages = 3

    fun onNext() {
        if (_page.value < totalPages - 1) {
            _page.value += 1
        }
    }

    fun onPrev() {
        if (_page.value > 0) {
            _page.value -= 1
        }
    }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingComplete(true)
            onDone()
        }
    }
}
