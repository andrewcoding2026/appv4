package com.nfc.security.domain.model

sealed class FreemiumState {
    data class Trial(val remainingMs: Long) : FreemiumState()
    object Expired : FreemiumState()
    object Premium : FreemiumState()
}
