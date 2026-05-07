package com.nfcsecurity.di

import com.nfcsecurity.data.repository.EventRepositoryImpl
import com.nfcsecurity.data.repository.FreemiumRepositoryImpl
import com.nfcsecurity.data.repository.NfcRepositoryImpl
import com.nfcsecurity.data.repository.ScanRepositoryImpl
import com.nfcsecurity.data.repository.SecurityRepositoryImpl
import com.nfcsecurity.data.repository.SessionRepositoryImpl
import com.nfcsecurity.data.repository.VaultRepositoryImpl
import com.nfcsecurity.data.repository.VpnRepositoryImpl
import com.nfcsecurity.domain.repository.EventRepository
import com.nfcsecurity.domain.repository.FreemiumRepository
import com.nfcsecurity.domain.repository.NfcRepository
import com.nfcsecurity.domain.repository.ScanRepository
import com.nfcsecurity.domain.repository.SecurityRepository
import com.nfcsecurity.domain.repository.SessionRepository
import com.nfcsecurity.domain.repository.VaultRepository
import com.nfcsecurity.domain.repository.VpnRepository
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
