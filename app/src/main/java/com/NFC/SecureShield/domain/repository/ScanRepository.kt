package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun performScan(): ScanReport
    suspend fun clearCaches(): Long
    fun getLastReport(): Flow<ScanReport?>
}
