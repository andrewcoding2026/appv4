package com.nfcsecurity.domain.usecase.scan

import com.nfcsecurity.domain.repository.ScanRepository
import javax.inject.Inject

class ClearAppCacheUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(): Long = repository.clearCaches()
}
