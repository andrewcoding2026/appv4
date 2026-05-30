package com.NFC.SecureShield.free.di

import com.NFC.SecureShield.free.data.repository.EventRepositoryImpl
import com.NFC.SecureShield.free.data.repository.FreemiumRepositoryImpl
import com.NFC.SecureShield.free.data.repository.NfcRepositoryImpl
import com.NFC.SecureShield.free.data.repository.ScanRepositoryImpl
import com.NFC.SecureShield.free.data.repository.SecurityRepositoryImpl
import com.NFC.SecureShield.free.data.repository.SessionRepositoryImpl
import com.NFC.SecureShield.free.data.repository.VaultRepositoryImpl
import com.NFC.SecureShield.free.data.repository.VpnRepositoryImpl
import com.NFC.SecureShield.free.domain.repository.EventRepository
import com.NFC.SecureShield.free.domain.repository.FreemiumRepository
import com.NFC.SecureShield.free.domain.repository.NfcRepository
import com.NFC.SecureShield.free.domain.repository.ScanRepository
import com.NFC.SecureShield.free.domain.repository.SecurityRepository
import com.NFC.SecureShield.free.domain.repository.SessionRepository
import com.NFC.SecureShield.free.domain.repository.VaultRepository
import com.NFC.SecureShield.free.domain.repository.VpnRepository
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
