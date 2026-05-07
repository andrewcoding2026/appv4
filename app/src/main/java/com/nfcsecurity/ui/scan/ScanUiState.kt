package com.nfcsecurity.ui.scan

import com.nfcsecurity.domain.model.ScanReport

data class ScanUiState(
    val isScanning: Boolean = false,
    val report: ScanReport? = null,
    val clearedBytes: Long? = null,
    val error: String? = null
)
