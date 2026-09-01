package com.fyllo.filemanager.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionService {
    private val keyAlias = "SafeFolderEncryptionKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        outputStream.write(iv.size)
        outputStream.write(iv)
        
        val cipherOutputStream = CipherOutputStream(outputStream, cipher)
        inputStream.copyTo(cipherOutputStream)
        cipherOutputStream.close()
    }

    fun decryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val ivSize = inputStream.read()
        if (ivSize <= 0 || ivSize > 32) {
            inputStream.copyTo(outputStream)
            return
        }
        val iv = ByteArray(ivSize)
        val readIv = inputStream.read(iv)
        if (readIv != ivSize) {
            inputStream.copyTo(outputStream)
            return
        }
        
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val cipherInputStream = CipherInputStream(inputStream, cipher)
            cipherInputStream.copyTo(outputStream)
            cipherInputStream.close()
        } catch (e: Exception) {
            // Fallback for unencrypted files
            outputStream.write(ivSize)
            outputStream.write(iv)
            inputStream.copyTo(outputStream)
        }
    }

    fun encryptFile(srcFile: java.io.File, destFile: java.io.File) {
        srcFile.inputStream().use { input ->
            destFile.outputStream().use { output ->
                encryptStream(input, output)
            }
        }
    }

    fun decryptFile(srcFile: java.io.File, destFile: java.io.File) {
        srcFile.inputStream().use { input ->
            destFile.outputStream().use { output ->
                decryptStream(input, output)
            }
        }
    }
}
