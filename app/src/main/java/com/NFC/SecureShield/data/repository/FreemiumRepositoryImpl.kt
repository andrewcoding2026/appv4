package com.NFC.SecureShield.data.repository

import android.util.Base64
import com.NFC.SecureShield.data.local.EncryptedPreferencesDataSource
import com.NFC.SecureShield.data.local.KeystoreCryptoDataSource
import com.NFC.SecureShield.domain.repository.FreemiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_TRIAL_START = "trial_start_ms"
private const val KEY_TRIAL_SIGNATURE = "trial_start_sig"
private const val KEY_IS_PREMIUM = "is_premium"

@Singleton
class FreemiumRepositoryImpl @Inject constructor(
    private val prefs: EncryptedPreferencesDataSource,
    private val crypto: KeystoreCryptoDataSource
) : FreemiumRepository {

    override suspend fun initTrialIfNeeded() = withContext(Dispatchers.IO) {
        if (!prefs.contains(KEY_TRIAL_START)) {
            val now = System.currentTimeMillis()
            val signature = crypto.sign(now.toByteArray())
            prefs.putLong(KEY_TRIAL_START, now)
            prefs.putString(KEY_TRIAL_SIGNATURE, Base64.encodeToString(signature, Base64.NO_WRAP))
        }
    }

    override suspend fun getTrialStartMs(): Long = withContext(Dispatchers.IO) {
        val stored = prefs.getLong(KEY_TRIAL_START)
        if (stored == -1L) return@withContext -1L
        val sigStr = prefs.getString(KEY_TRIAL_SIGNATURE) ?: return@withContext -1L
        val sig = Base64.decode(sigStr, Base64.NO_WRAP)
        if (!crypto.verify(stored.toByteArray(), sig)) {
            return@withContext Long.MIN_VALUE
        }
        stored
    }

    override suspend fun isPremium(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_IS_PREMIUM)
    }

    override suspend fun setPremium() = withContext(Dispatchers.IO) {
        prefs.putBoolean(KEY_IS_PREMIUM, true)
    }

    private fun Long.toByteArray(): ByteArray {
        val bytes = ByteArray(8)
        for (i in 7 downTo 0) {
            bytes[i] = (this shr ((7 - i) * 8) and 0xFF).toByte()
        }
        return bytes
    }
}
