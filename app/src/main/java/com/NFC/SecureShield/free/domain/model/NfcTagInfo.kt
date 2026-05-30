package com.NFC.SecureShield.free.domain.model

data class NfcTagInfo(
    val id: String,
    val techList: List<String>,
    val type: NfcTagType,
    val ndefRecords: List<NdefRecordInfo>,
    val discoveredAt: Long
)
