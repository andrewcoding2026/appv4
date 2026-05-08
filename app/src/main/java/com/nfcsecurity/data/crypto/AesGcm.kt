package com.nfcsecurity.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AesGcm @Inject constructor() {
    // v2: setUserAuthenticationRequired=true, validity=-1 (biometric required per-use via CryptoObject)
    private val keyAlias = "nfc_security_vault_v2"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

    private fun getOrCreateKey(): SecretKey {
        keyStore.getKey(keyAlias, null)?.let { return it as SecretKey }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(-1, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    /** Returns a Cipher initialized for encryption. Wrap in [BiometricPrompt.CryptoObject] before authenticating. */
    fun prepareEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher
    }

    /** Encrypts [plaintext] using a Cipher that has been authenticated via [BiometricPrompt.CryptoObject]. */
    fun encryptWithCipher(cipher: Cipher, plaintext: ByteArray): Pair<ByteArray, ByteArray> =
        cipher.doFinal(plaintext) to cipher.iv

    /** Returns a Cipher initialized for decryption of [iv]. Wrap in [BiometricPrompt.CryptoObject] before authenticating. */
    fun prepareDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher
    }

    /** Decrypts [ciphertext] using a Cipher that has been authenticated via [BiometricPrompt.CryptoObject]. */
    fun decryptWithCipher(cipher: Cipher, ciphertext: ByteArray): ByteArray? =
        try {
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Log.e("AesGcm", "Decryption failed", e)
            null
        }
}