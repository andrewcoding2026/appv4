package com.nfc.security.data.repository

import android.nfc.NfcAdapter
import com.nfc.security.domain.model.NfcTagInfo
import com.nfc.security.domain.repository.NfcRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcRepositoryImpl @Inject constructor(
    private val nfcAdapter: NfcAdapter?
) : NfcRepository {

    private val _lastTag = MutableStateFlow<NfcTagInfo?>(null)
    private val _tagStream = MutableSharedFlow<NfcTagInfo>(replay = 1)

    override fun observeNfcEnabled(): Flow<Boolean> = flow {
        while (true) {
            emit(nfcAdapter?.isEnabled == true)
            delay(2_000)
        }
    }

    override fun getLastDiscoveredTag(): Flow<NfcTagInfo?> = _tagStream.asSharedFlow()

    override suspend fun publishTag(tag: NfcTagInfo) {
        _lastTag.value = tag
        _tagStream.emit(tag)
    }
}
