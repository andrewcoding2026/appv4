package com.nfcsecurity.ui.nfc

import com.nfcsecurity.domain.model.NfcTagInfo

data class NfcMonitorUiState(
    val isNfcEnabled: Boolean = false,
    val isNfcSupported: Boolean = true,
    val lastTag: NfcTagInfo? = null,
    val tagHistory: List<NfcTagInfo> = emptyList()
)
