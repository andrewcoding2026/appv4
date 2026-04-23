package com.nfc.security.domain.usecase.freemium

import com.nfc.security.domain.model.FreemiumState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveFreemiumStateUseCase @Inject constructor(
    private val getFreemiumState: GetFreemiumStateUseCase
) {
    operator fun invoke(): Flow<FreemiumState> = flow {
        while (true) {
            emit(getFreemiumState())
            delay(60_000L)
        }
    }
}
