package com.NFC.SecureShield.free.domain.usecase.scan

import com.NFC.SecureShield.free.domain.repository.ScanRepository
import javax.inject.Inject

class ClearAppCacheUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(): Long = repository.clearCaches()
}
