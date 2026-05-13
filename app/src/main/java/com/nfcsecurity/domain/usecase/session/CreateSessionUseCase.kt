package com.nfcsecurity.domain.usecase.session

import android.util.Base64
import com.nfcsecurity.data.local.KeystoreCryptoDataSource
import com.nfcsecurity.domain.model.FreemiumState
import com.nfcsecurity.domain.model.SessionToken
import com.nfcsecurity.domain.repository.SessionRepository
import com.nfcsecurity.domain.usecase.freemium.GetFreemiumStateUseCase
import java.util.UUID
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val crypto: KeystoreCryptoDataSource,
    private val getFreemiumState: GetFreemiumStateUseCase
) {
    suspend operator fun invoke(): SessionToken {
        val now = System.currentTimeMillis()
        val freemiumState = getFreemiumState()
        val expiresAt = when (freemiumState) {
            is FreemiumState.Trial -> now + freemiumState.remainingMs
            FreemiumState.Premium -> now + (365L * 24 * 60 * 60 * 1000)
            FreemiumState.Expired -> now
        }
        val token = UUID.randomUUID().toString()
        val signatureInput = "$token:$now:$expiresAt".toByteArray()
        val signature = Base64.encodeToString(crypto.sign(signatureInput), Base64.NO_WRAP)
        val sessionToken = SessionToken(
            token = token,
            createdAt = now,
            expiresAt = expiresAt,
            signature = signature
        )
        sessionRepository.saveSession(sessionToken)
        return sessionToken
    }
}
