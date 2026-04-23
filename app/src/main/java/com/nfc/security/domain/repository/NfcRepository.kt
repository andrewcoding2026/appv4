package com.nfc.security.domain.repository

import com.nfc.security.domain.model.NfcTagInfo
import kotlinx.coroutines.flow.Flow

interface NfcRepository {
    fun observeNfcEnabled(): Flow<Boolean>
    fun getLastDiscoveredTag(): Flow<NfcTagInfo?>
    suspend fun publishTag(tag: NfcTagInfo)
}
