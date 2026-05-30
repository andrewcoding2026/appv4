package com.NFC.SecureShield.data.repository

import android.util.Base64
import com.NFC.SecureShield.data.local.EncryptedPreferencesDataSource
import com.NFC.SecureShield.data.local.KeystoreCryptoDataSource
import com.NFC.SecureShield.domain.model.SessionToken
import com.NFC.SecureShield.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_SESSION = "session_token"

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val prefs: EncryptedPreferencesDataSource,
    private val crypto: KeystoreCryptoDataSource
) : SessionRepository {

    // Session payload is AES-256-GCM encrypted under a Keystore-backed key. On a rooted
    // device the backing store can be tampered or the Keystore call intercepted; see
    // ValidateSessionUseCase for the full PoC-scope limitation and production guidance.
    override suspend fun saveSession(token: SessionToken) = withContext(Dispatchers.IO) {
        val json = """{"token":"${token.token}","createdAt":${token.createdAt},"expiresAt":${token.expiresAt},"signature":"${token.signature}"}"""
        val (iv, ciphertext) = crypto.encrypt(json.toByteArray())
        val encoded = Base64.encodeToString(iv, Base64.NO_WRAP) + "|" + Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        prefs.putString(KEY_SESSION, encoded)
    }

    override suspend fun getSession(): SessionToken? = withContext(Dispatchers.IO) {
        val encoded = prefs.getString(KEY_SESSION) ?: return@withContext null
        try {
            val parts = encoded.split("|")
            if (parts.size != 2) return@withContext null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val decryptedBytes = crypto.decrypt(iv, ciphertext) ?: return@withContext null
            val json = String(decryptedBytes)
            parseSession(json)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearSession() = withContext(Dispatchers.IO) {
        prefs.remove(KEY_SESSION)
    }

    private fun parseSession(json: String): SessionToken {
        fun extractString(key: String): String {
            val pattern = "\"$key\":\"([^\"]*)\"".toRegex()
            return pattern.find(json)?.groupValues?.get(1) ?: ""
        }
        fun extractLong(key: String): Long {
            val pattern = "\"$key\":(\\d+)".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toLong() ?: 0L
        }
        return SessionToken(
            token = extractString("token"),
            createdAt = extractLong("createdAt"),
            expiresAt = extractLong("expiresAt"),
            signature = extractString("signature")
        )
    }
}
