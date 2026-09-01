package com.fyllo.filemanager.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM encryption backed by Android Keystore.
 * Keys are hardware-backed and never exposed in plaintext.
 */
object CryptoManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS_SAFE_FOLDER = "SFileManager_SafeFolder_Key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    /** Generate (or retrieve) the AES-256 key for Safe Folder encryption. */
    private fun getOrCreateKey(alias: String = KEY_ALIAS_SAFE_FOLDER): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false) // PIN enforced at app layer
                .build()
        )
        return keyGenerator.generateKey()
    }

    /**
     * Encrypt [plaintext] bytes.
     * Returns [IV (12 bytes)] + [ciphertext + GCM tag].
     */
    fun encrypt(plaintext: ByteArray, alias: String = KEY_ALIAS_SAFE_FOLDER): ByteArray {
        val key = getOrCreateKey(alias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv // GCM IV (12 bytes)
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    /**
     * Decrypt bytes produced by [encrypt].
     * Input must be [IV (12 bytes)] + [ciphertext + GCM tag].
     */
    fun decrypt(data: ByteArray, alias: String = KEY_ALIAS_SAFE_FOLDER): ByteArray {
        val key = getOrCreateKey(alias)
        val iv = data.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = data.copyOfRange(GCM_IV_LENGTH, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    /** Delete the Keystore entry (e.g., on reset). */
    fun deleteKey(alias: String = KEY_ALIAS_SAFE_FOLDER) {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
    }
}
