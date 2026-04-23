package com.nfc.security.ui.nfc

import com.nfc.security.domain.model.NfcTagInfo

data class NfcMonitorUiState(
    val isNfcEnabled: Boolean = false,
    val isNfcSupported: Boolean = true,
    val lastTag: NfcTagInfo? = null,
    val tagHistory: List<NfcTagInfo> = emptyList()
)
