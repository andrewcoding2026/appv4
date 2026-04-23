package com.nfc.security.domain.repository

import com.nfc.security.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun performScan(): ScanReport
    suspend fun clearCaches(): Long
    fun getLastReport(): Flow<ScanReport?>
}
