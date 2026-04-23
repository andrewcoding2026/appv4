package com.nfc.security.domain.repository

interface FreemiumRepository {
    suspend fun getTrialStartMs(): Long
    suspend fun isPremium(): Boolean
    suspend fun setPremium()
    suspend fun initTrialIfNeeded()
}
