package com.fyllo.filemanager.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Manages PIN storage (PBKDF2-SHA256 hashed) and App Lock state using
 * EncryptedSharedPreferences — encrypted with AES-256 via Android Keystore.
 */
class AppLockManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_lock_secure_prefs"
        private const val KEY_SAFE_FOLDER_HASH = "safe_folder_pin_hash"
        private const val KEY_SAFE_FOLDER_SALT = "safe_folder_pin_salt"
        private const val KEY_APP_LOCK_HASH = "app_lock_pin_hash"
        private const val KEY_APP_LOCK_SALT = "app_lock_pin_salt"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LOCK_TIMEOUT_SECS = "lock_timeout_secs"
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ─── PIN hashing ──────────────────────────────────────────────────────────

    private fun generateSalt(): ByteArray = ByteArray(16).also {
        java.security.SecureRandom().nextBytes(it)
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.fromHex(): ByteArray {
        val len = length
        return ByteArray(len / 2) { i -> ((this[i * 2].digitToInt(16) shl 4) + this[i * 2 + 1].digitToInt(16)).toByte() }
    }

    // ─── Safe Folder PIN ──────────────────────────────────────────────────────

    fun hasSafeFolderPin(): Boolean = prefs.contains(KEY_SAFE_FOLDER_HASH)

    fun setSafeFolderPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_SAFE_FOLDER_HASH, hash.toHex())
            .putString(KEY_SAFE_FOLDER_SALT, salt.toHex())
            .apply()
    }

    fun verifySafeFolderPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_SAFE_FOLDER_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_SAFE_FOLDER_SALT, null) ?: return false
        val inputHash = hashPin(pin, storedSalt.fromHex())
        return inputHash.toHex() == storedHash
    }

    fun clearSafeFolderPin() {
        prefs.edit()
            .remove(KEY_SAFE_FOLDER_HASH)
            .remove(KEY_SAFE_FOLDER_SALT)
            .apply()
    }

    // ─── App Lock PIN ─────────────────────────────────────────────────────────

    fun hasAppLockPin(): Boolean = prefs.contains(KEY_APP_LOCK_HASH)

    fun setAppLockPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_APP_LOCK_HASH, hash.toHex())
            .putString(KEY_APP_LOCK_SALT, salt.toHex())
            .apply()
    }

    fun verifyAppLockPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_APP_LOCK_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_APP_LOCK_SALT, null) ?: return false
        val inputHash = hashPin(pin, storedSalt.fromHex())
        return inputHash.toHex() == storedHash
    }

    // ─── App Lock Settings ────────────────────────────────────────────────────

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    /** Lock timeout in seconds. Default 30s. */
    var lockTimeoutSeconds: Int
        get() = prefs.getInt(KEY_LOCK_TIMEOUT_SECS, 30)
        set(value) = prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECS, value).apply()
}
