package com.nfc.security.domain.repository

import com.nfc.security.domain.model.SessionToken

interface SessionRepository {
    suspend fun saveSession(token: SessionToken)
    suspend fun getSession(): SessionToken?
    suspend fun clearSession()
}
