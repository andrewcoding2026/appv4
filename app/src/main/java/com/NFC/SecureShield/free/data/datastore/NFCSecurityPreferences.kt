package com.NFC.SecureShield.free.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFCSecurityPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val ENTITLEMENT = stringPreferencesKey("entitlement")
        val TRIAL_STARTED_AT = longPreferencesKey("trial_started_at")
        val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
        val MODULE_NFC_ENABLED = booleanPreferencesKey("module_nfc_enabled")
        val MODULE_VPN_ENABLED = booleanPreferencesKey("module_vpn_enabled")
        val MODULE_SCAN_ENABLED = booleanPreferencesKey("module_scan_enabled")
        val MODULE_VAULT_ENABLED = booleanPreferencesKey("module_vault_enabled")
        val MODULE_INTEGRITY_ENABLED = booleanPreferencesKey("module_integrity_enabled")
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }
    val entitlement: Flow<String> = dataStore.data.map { it[Keys.ENTITLEMENT] ?: "none" }
    val autoLockMinutes: Flow<Int> = dataStore.data.map { it[Keys.AUTO_LOCK_MINUTES] ?: 5 }
    val moduleNfcEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MODULE_NFC_ENABLED] ?: true }
    val moduleVpnEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MODULE_VPN_ENABLED] ?: true }
    val moduleScanEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MODULE_SCAN_ENABLED] ?: true }
    val moduleVaultEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MODULE_VAULT_ENABLED] ?: true }
    val moduleIntegrityEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.MODULE_INTEGRITY_ENABLED] ?: true }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = value }
    }

    suspend fun setEntitlement(value: String) {
        dataStore.edit { it[Keys.ENTITLEMENT] = value }
    }

    suspend fun setAutoLockMinutes(value: Int) {
        dataStore.edit { it[Keys.AUTO_LOCK_MINUTES] = value }
    }

    suspend fun setModuleEnabled(key: Preferences.Key<Boolean>, enabled: Boolean) {
        dataStore.edit { it[key] = enabled }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
