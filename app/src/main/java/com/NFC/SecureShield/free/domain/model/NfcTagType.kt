package com.NFC.SecureShield.free.domain.model

sealed class NfcTagType {
    object Iso14443A : NfcTagType()
    object Iso14443B : NfcTagType()
    object Iso15693 : NfcTagType()
    object Iso18092 : NfcTagType()
    object MifareClassic : NfcTagType()
    object MifareUltralight : NfcTagType()
    object Ndef : NfcTagType()
    object Unknown : NfcTagType()
}
