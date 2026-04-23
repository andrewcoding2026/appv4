package com.nfc.security.domain.model

sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    data class Connected(
        val serverIp: String,
        val bytesIn: Long,
        val bytesOut: Long
    ) : VpnState()
    data class Error(val message: String) : VpnState()
}
