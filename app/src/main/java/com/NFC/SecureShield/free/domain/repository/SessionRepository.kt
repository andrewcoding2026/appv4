package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.domain.model.SessionToken

interface SessionRepository {
    suspend fun saveSession(token: SessionToken)
    suspend fun getSession(): SessionToken?
    suspend fun clearSession()
}
