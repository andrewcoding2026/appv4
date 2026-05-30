package com.NFC.SecureShield.ui.scan

import com.NFC.SecureShield.domain.model.ScanReport

data class ScanUiState(
    val isScanning: Boolean = false,
    val report: ScanReport? = null,
    val clearedBytes: Long? = null,
    val error: String? = null
)
