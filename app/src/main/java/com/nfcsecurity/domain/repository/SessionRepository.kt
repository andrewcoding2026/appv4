package com.nfcsecurity.domain.repository

import com.nfcsecurity.domain.model.SessionToken

interface SessionRepository {
    suspend fun saveSession(token: SessionToken)
    suspend fun getSession(): SessionToken?
    suspend fun clearSession()
}
