package com.NFC.SecureShield.domain.usecase.scan

import com.NFC.SecureShield.domain.repository.ScanRepository
import javax.inject.Inject

class ClearAppCacheUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(): Long = repository.clearCaches()
}
