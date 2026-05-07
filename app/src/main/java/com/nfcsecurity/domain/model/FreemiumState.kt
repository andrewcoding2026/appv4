package com.nfcsecurity.domain.model

sealed class FreemiumState {
    data class Trial(val remainingMs: Long) : FreemiumState()
    object Expired : FreemiumState()
    object Premium : FreemiumState()
}
