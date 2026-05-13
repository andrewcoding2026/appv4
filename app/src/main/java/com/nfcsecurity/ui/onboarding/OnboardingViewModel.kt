package com.nfcsecurity.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcsecurity.data.datastore.NFCSecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: NFCSecurityPreferences
) : ViewModel() {

    val totalPages = 3

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setOnboardingComplete(true)
            onDone()
        }
    }
}
