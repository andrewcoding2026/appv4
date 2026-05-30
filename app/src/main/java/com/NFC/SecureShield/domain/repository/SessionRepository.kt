package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.domain.model.SessionToken

interface SessionRepository {
    suspend fun saveSession(token: SessionToken)
    suspend fun getSession(): SessionToken?
    suspend fun clearSession()
}
