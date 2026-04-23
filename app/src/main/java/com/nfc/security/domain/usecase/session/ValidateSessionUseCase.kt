package com.nfc.security.domain.usecase.session

import android.util.Base64
import com.nfc.security.data.local.KeystoreCryptoDataSource
import com.nfc.security.domain.repository.SessionRepository
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val crypto: KeystoreCryptoDataSource
) {
    suspend operator fun invoke(): Boolean {
        val session = sessionRepository.getSession() ?: return false
        if (System.currentTimeMillis() > session.expiresAt) return false
        val signatureInput = "${session.token}${session.createdAt}${session.expiresAt}".toByteArray()
        val storedSig = Base64.decode(session.signature, Base64.NO_WRAP)
        return crypto.verify(signatureInput, storedSig)
    }
}
