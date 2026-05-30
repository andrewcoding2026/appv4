package com.NFC.SecureShield.domain.usecase.nfc

import com.NFC.SecureShield.domain.repository.NfcRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNfcStateUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeNfcEnabled()
}
