package com.NFC.SecureShield.di

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.NFC.SecureShield.data.db.NFCSecurityDatabase
import com.NFC.SecureShield.data.db.EventDao
import com.NFC.SecureShield.data.db.VaultItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "NFCSecurity_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNfcAdapter(@ApplicationContext context: Context): NfcAdapter? {
        val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
        return nfcManager?.defaultAdapter
    }

    @Provides
    @Singleton
    fun provideKeyguardManager(@ApplicationContext context: Context): KeyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager =
        context.packageManager

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideNFCSecurityDatabase(@ApplicationContext context: Context): NFCSecurityDatabase =
        Room.databaseBuilder(context, NFCSecurityDatabase::class.java, "NFCSecurity.db").build()

    @Provides
    @Singleton
    fun provideEventDao(db: NFCSecurityDatabase): EventDao = db.eventDao()

    @Provides
    @Singleton
    fun provideVaultItemDao(db: NFCSecurityDatabase): VaultItemDao = db.vaultItemDao()
}
