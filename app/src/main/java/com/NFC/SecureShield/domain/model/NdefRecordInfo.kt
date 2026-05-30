package com.NFC.SecureShield.domain.model

data class NdefRecordInfo(
    val tnf: Short,
    val type: ByteArray,
    val payload: ByteArray,
    val payloadText: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NdefRecordInfo) return false
        return tnf == other.tnf &&
            type.contentEquals(other.type) &&
            payload.contentEquals(other.payload) &&
            payloadText == other.payloadText
    }

    override fun hashCode(): Int {
        var result = tnf.toInt()
        result = 31 * result + type.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + (payloadText?.hashCode() ?: 0)
        return result
    }
}
