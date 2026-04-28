package com.nfc.security.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfc.security.data.datastore.AegisPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val moduleNfc: Boolean = true,
    val moduleVpn: Boolean = true,
    val moduleScan: Boolean = true,
    val moduleVault: Boolean = true,
    val moduleIntegrity: Boolean = true,
    val autoLockMinutes: Int = 5,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AegisPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.moduleNfcEnabled,
                prefs.moduleVpnEnabled,
                prefs.moduleScanEnabled,
                prefs.moduleVaultEnabled,
                prefs.moduleIntegrityEnabled,
            ) { nfc, vpn, scan, vault, integrity ->
                _uiState.update {
                    it.copy(
                        moduleNfc = nfc,
                        moduleVpn = vpn,
                        moduleScan = scan,
                        moduleVault = vault,
                        moduleIntegrity = integrity,
                    )
                }
            }.collect {}
        }
        viewModelScope.launch {
            prefs.autoLockMinutes.collect { minutes ->
                _uiState.update { it.copy(autoLockMinutes = minutes) }
            }
        }
    }

    fun setNfcEnabled(v: Boolean) = toggle(AegisPreferences.Keys.MODULE_NFC_ENABLED, v)
    fun setVpnEnabled(v: Boolean) = toggle(AegisPreferences.Keys.MODULE_VPN_ENABLED, v)
    fun setScanEnabled(v: Boolean) = toggle(AegisPreferences.Keys.MODULE_SCAN_ENABLED, v)
    fun setVaultEnabled(v: Boolean) = toggle(AegisPreferences.Keys.MODULE_VAULT_ENABLED, v)
    fun setIntegrityEnabled(v: Boolean) = toggle(AegisPreferences.Keys.MODULE_INTEGRITY_ENABLED, v)

    private fun toggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, v: Boolean) {
        viewModelScope.launch { prefs.setModuleEnabled(key, v) }
    }

    fun clearAll() {
        viewModelScope.launch { prefs.clearAll() }
    }
}
