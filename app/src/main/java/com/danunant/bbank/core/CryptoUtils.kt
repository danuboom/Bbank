package com.danunant.bbank.core

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64 // Use Android's Base64

object CryptoUtils {

    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 65536 // Standard iteration count
    private const val KEY_LENGTH = 128 // 128-bit hash output
    private const val SALT_SIZE = 16 // 16 bytes for salt

    // Generates a random salt
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)
        return salt
    }

    // Hashes the PIN using the salt
    fun hashPin(pin: CharArray, salt: ByteArray): ByteArray {
        val spec: KeySpec = PBEKeySpec(pin, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    // Verifies a PIN attempt against the stored hash and salt
    fun verifyPin(pinAttempt: CharArray, storedHash: ByteArray, salt: ByteArray): Boolean {
        val hashAttempt = hashPin(pinAttempt, salt)
        return storedHash.contentEquals(hashAttempt) // Use contentEquals for ByteArray comparison
    }

    // --- Helpers to store/retrieve byte arrays as Base64 strings (easier for DB) ---
    fun encodeToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeFromBase64(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.NO_WRAP)
    }
}