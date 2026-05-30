package com.NFC.SecureShield.free.ui.scan

import com.NFC.SecureShield.free.domain.model.ScanReport

data class ScanUiState(
    val isScanning: Boolean = false,
    val report: ScanReport? = null,
    val clearedBytes: Long? = null,
    val error: String? = null
)
