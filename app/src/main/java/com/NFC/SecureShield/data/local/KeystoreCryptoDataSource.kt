package com.NFC.SecureShield.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val AES_KEY_ALIAS = "nfc_security_aes_key"
private const val HMAC_KEY_ALIAS = "nfc_security_hmac_key"
private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH = 128

@Singleton
class KeystoreCryptoDataSource @Inject constructor() {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
    }

    // setUserAuthenticationRequired is intentionally absent here. This key protects general
    // app data (scan records, session tokens) that must be readable without user interaction
    // (e.g. background WorkManager jobs). The vault key in AesGcm uses a separate alias
    // ("nfc_security_vault_v2") with setUserAuthenticationRequired(true) and per-use biometric
    // authentication via CryptoObject — these are architecturally distinct trust boundaries.
    private fun getOrCreateAesKey(): SecretKey {
        return if (keyStore.containsAlias(AES_KEY_ALIAS)) {
            (keyStore.getEntry(AES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGen.generateKey()
        }
    }

    private fun getOrCreateHmacKey(): SecretKey {
        return if (keyStore.containsAlias(HMAC_KEY_ALIAS)) {
            (keyStore.getEntry(HMAC_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, KEYSTORE_PROVIDER)
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    HMAC_KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).build()
            )
            keyGen.generateKey()
        }
    }

    fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return Pair(iv, ciphertext)
    }

    fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), spec)
            cipher.doFinal(ciphertext)
        } catch (e: AEADBadTagException) {
            Log.e("KeystoreCryptoDataSource", "Decryption failed: AEADBadTagException", e)
            null
        } catch (e: Exception) {
            Log.e("KeystoreCryptoDataSource", "Decryption failed", e)
            null
        }
    }

    fun sign(data: ByteArray): ByteArray {
        val mac = Mac.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256)
        mac.init(getOrCreateHmacKey())
        return mac.doFinal(data)
    }

    fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val expected = sign(data)
        return MessageDigest.isEqual(expected, signature)
    }
}
