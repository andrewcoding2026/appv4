package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun performScan(): ScanReport
    suspend fun clearCaches(): Long
    fun getLastReport(): Flow<ScanReport?>
}
