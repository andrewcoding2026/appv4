package com.NFC.SecureShield.free.domain.model

data class SessionToken(
    val token: String,
    val createdAt: Long,
    val expiresAt: Long,
    val signature: String
)
