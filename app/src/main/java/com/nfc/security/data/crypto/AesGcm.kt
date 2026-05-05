package com.nfc.security.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AesGcm @Inject constructor() {
    private val keyAlias = "nfc_security_vault_v1"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

    private fun getOrCreateKey(): SecretKey {
        keyStore.getKey(keyAlias, null)?.let { return it as SecretKey }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext = cipher.doFinal(plaintext)
        return ciphertext to cipher.iv
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray? {
        return try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            cipher.doFinal(ciphertext)
        } catch (e: AEADBadTagException) {
            Log.e("AesGcm", "Decryption failed: AEADBadTagException. Data might be corrupted or key changed.", e)
            null
        } catch (e: Exception) {
            Log.e("AesGcm", "Decryption failed", e)
            null
        }
    }
}
