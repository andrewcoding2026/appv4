package com.nfc.security.di

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import androidx.biometric.BiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
}
