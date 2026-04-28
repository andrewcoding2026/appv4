package com.nfc.security.ui.navigation

object NavRoutes {
    const val ONBOARDING       = "onboarding"
    const val DASHBOARD        = "dashboard"
    const val NOTIFICATIONS    = "notifications"
    const val NFC_SENTINEL     = "nfc_sentinel"
    const val TUNNEL           = "tunnel"
    const val SCAN             = "scan"
    const val VAULT            = "vault"
    const val INTEGRITY        = "integrity"
    const val INCIDENT_DETAIL  = "incident/{eventId}"
    const val PAYWALL          = "paywall"
    const val SETTINGS         = "settings"

    fun incidentDetail(eventId: Long) = "incident/$eventId"

    val bottomTabRoutes = setOf(DASHBOARD, TUNNEL, SCAN, VAULT, INTEGRITY)
}
