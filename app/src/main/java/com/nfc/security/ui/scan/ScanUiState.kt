package com.nfc.security.ui.scan

import com.nfc.security.domain.model.ScanReport

data class ScanUiState(
    val isScanning: Boolean = false,
    val report: ScanReport? = null,
    val clearedBytes: Long? = null,
    val error: String? = null
)
