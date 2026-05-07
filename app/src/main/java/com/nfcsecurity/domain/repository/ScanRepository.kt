package com.nfcsecurity.domain.repository

import com.nfcsecurity.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun performScan(): ScanReport
    suspend fun clearCaches(): Long
    fun getLastReport(): Flow<ScanReport?>
}
