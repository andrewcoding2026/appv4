package com.NFC.SecureShield.domain.usecase.session

import android.util.Base64
import com.NFC.SecureShield.data.local.KeystoreCryptoDataSource
import com.NFC.SecureShield.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * PoC Limitation — device-local validation only.
 *
 * The entire trust chain (HMAC key in Android Keystore, session payload in
 * EncryptedSharedPreferences) resides on the device. An adversary with root access can
 * subvert this validation in two ways:
 *
 *   1. Storage tampering — directly modify the EncryptedSharedPreferences backing file
 *      to extend expiresAt, then replay the stored (or forged) signature.
 *   2. Keystore interception — hook the Keystore HMAC operation at the framework or
 *      native layer (e.g. via Frida) to return a forged valid signature for any input.
 *
 * Both vectors are acknowledged constraints of the PoC scope. The HMAC provides integrity
 * against casual tampering on non-rooted devices and serves as a documented cryptographic
 * control for the thesis. Production deployment must replace or supplement this check with
 * an authoritative server-side validation endpoint that issues short-lived, server-signed
 * session tokens and verifies purchase receipts via the Google Play Billing API.
 */
class ValidateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val crypto: KeystoreCryptoDataSource
) {
    suspend operator fun invoke(): Boolean {
        val session = sessionRepository.getSession() ?: return false
        if (System.currentTimeMillis() > session.expiresAt) return false
        val signatureInput = "${session.token}:${session.createdAt}:${session.expiresAt}".toByteArray()
        val storedSig = Base64.decode(session.signature, Base64.NO_WRAP)
        return crypto.verify(signatureInput, storedSig)
    }
}
