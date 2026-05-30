package com.NFC.SecureShield.ui.vpn

import android.content.Intent

sealed class VpnEvent {
    data class RequestVpnPermission(val intent: Intent) : VpnEvent()
    data class ShowError(val message: String) : VpnEvent()
}
