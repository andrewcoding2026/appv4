package com.nfc.security.di

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
import com.nfc.security.data.db.AegisDatabase
import com.nfc.security.data.db.EventDao
import com.nfc.security.data.db.VaultItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aegis_prefs")

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
    fun provideAegisDatabase(@ApplicationContext context: Context): AegisDatabase =
        Room.databaseBuilder(context, AegisDatabase::class.java, "aegis.db").build()

    @Provides
    @Singleton
    fun provideEventDao(db: AegisDatabase): EventDao = db.eventDao()

    @Provides
    @Singleton
    fun provideVaultItemDao(db: AegisDatabase): VaultItemDao = db.vaultItemDao()
}
