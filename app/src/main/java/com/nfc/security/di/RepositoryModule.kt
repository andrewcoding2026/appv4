package com.nfc.security.di

import com.nfc.security.data.repository.EventRepositoryImpl
import com.nfc.security.data.repository.FreemiumRepositoryImpl
import com.nfc.security.data.repository.NfcRepositoryImpl
import com.nfc.security.data.repository.ScanRepositoryImpl
import com.nfc.security.data.repository.SecurityRepositoryImpl
import com.nfc.security.data.repository.SessionRepositoryImpl
import com.nfc.security.data.repository.VaultRepositoryImpl
import com.nfc.security.data.repository.VpnRepositoryImpl
import com.nfc.security.domain.repository.EventRepository
import com.nfc.security.domain.repository.FreemiumRepository
import com.nfc.security.domain.repository.NfcRepository
import com.nfc.security.domain.repository.ScanRepository
import com.nfc.security.domain.repository.SecurityRepository
import com.nfc.security.domain.repository.SessionRepository
import com.nfc.security.domain.repository.VaultRepository
import com.nfc.security.domain.repository.VpnRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindNfcRepository(impl: NfcRepositoryImpl): NfcRepository

    @Binds @Singleton
    abstract fun bindVpnRepository(impl: VpnRepositoryImpl): VpnRepository

    @Binds @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    @Binds @Singleton
    abstract fun bindFreemiumRepository(impl: FreemiumRepositoryImpl): FreemiumRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository
}
