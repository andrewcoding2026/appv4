package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.domain.model.NfcTagInfo
import kotlinx.coroutines.flow.Flow

interface NfcRepository {
    fun observeNfcEnabled(): Flow<Boolean>
    fun getLastDiscoveredTag(): Flow<NfcTagInfo?>
    suspend fun publishTag(tag: NfcTagInfo)
}
