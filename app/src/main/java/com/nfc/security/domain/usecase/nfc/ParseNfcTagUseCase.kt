package com.nfc.security.domain.usecase.nfc

import android.nfc.Tag
import android.nfc.tech.Ndef
import com.nfc.security.domain.model.NdefRecordInfo
import com.nfc.security.domain.model.NfcTagInfo
import com.nfc.security.domain.model.NfcTagType
import javax.inject.Inject

class ParseNfcTagUseCase @Inject constructor() {

    operator fun invoke(tag: Tag): NfcTagInfo {
        val id = tag.id?.joinToString("") { "%02X".format(it) } ?: "unknown"
        val techList = tag.techList?.toList() ?: emptyList()
        val type = resolveType(techList)
        val ndefRecords = parseNdefRecords(tag)
        return NfcTagInfo(
            id = id,
            techList = techList.map { it.substringAfterLast('.') },
            type = type,
            ndefRecords = ndefRecords,
            discoveredAt = System.currentTimeMillis()
        )
    }

    private fun resolveType(techList: List<String>): NfcTagType = when {
        techList.any { it.contains("MifareClassic") } -> NfcTagType.MifareClassic
        techList.any { it.contains("MifareUltralight") } -> NfcTagType.MifareUltralight
        techList.any { it.contains("IsoDep") } -> NfcTagType.Iso14443A
        techList.any { it.contains("NfcA") } -> NfcTagType.Iso14443A
        techList.any { it.contains("NfcB") } -> NfcTagType.Iso14443B
        techList.any { it.contains("NfcF") } -> NfcTagType.Iso18092
        techList.any { it.contains("NfcV") } -> NfcTagType.Iso15693
        techList.any { it.contains("Ndef") } -> NfcTagType.Ndef
        else -> NfcTagType.Unknown
    }

    private fun parseNdefRecords(tag: Tag): List<NdefRecordInfo> {
        return try {
            val ndef = Ndef.get(tag) ?: return emptyList()
            ndef.connect()
            val message = ndef.ndefMessage
            ndef.close()
            message?.records?.map { record ->
                val payloadText = when (record.tnf) {
                    android.nfc.NdefRecord.TNF_WELL_KNOWN if record.type.contentEquals(android.nfc.NdefRecord.RTD_TEXT) &&
                            record.payload.isNotEmpty()
                        -> {
                        val languageCodeLength = record.payload[0].toInt() and 0x3F
                        String(
                            record.payload,
                            1 + languageCodeLength,
                            record.payload.size - 1 - languageCodeLength,
                            Charsets.UTF_8
                        )
                    }

                    android.nfc.NdefRecord.TNF_WELL_KNOWN if record.type.contentEquals(android.nfc.NdefRecord.RTD_URI)
                        -> {
                        record.toUri()?.toString()
                    }

                    else -> null
                }
                NdefRecordInfo(
                    tnf = record.tnf,
                    type = record.type ?: ByteArray(0),
                    payload = record.payload ?: ByteArray(0),
                    payloadText = payloadText
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
