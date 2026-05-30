package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.domain.model.NfcTagInfo
import kotlinx.coroutines.flow.Flow

interface NfcRepository {
    fun observeNfcEnabled(): Flow<Boolean>
    fun getLastDiscoveredTag(): Flow<NfcTagInfo?>
    suspend fun publishTag(tag: NfcTagInfo)
}
