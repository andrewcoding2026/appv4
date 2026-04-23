package com.nfc.security.domain.model

data class ScanReport(
    val hits: List<MalwareHit>,
    val scannedApps: Int,
    val scannedFiles: Int,
    val riskScore: Int,
    val scanCompletedAt: Long
)
