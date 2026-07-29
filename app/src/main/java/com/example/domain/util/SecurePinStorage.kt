package com.example.domain.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.Locale

/**
 * Secure local encrypted PIN storage utility using SHA-256 message digest with salt
 * for authenticating staff members before accessing the POS.
 */
object SecurePinStorage {

    private const val PREF_NAME = "swadsutra_secure_staff_pins"
    private const val SALT = "SWAD_SUTRA_FINE_DINING_SALT_2026_ASTRA"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Hashes a PIN string securely with SHA-256 and a secret salt
     */
    fun hashPin(pin: String): String {
        val saltedInput = "$SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedInput.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Stores an encrypted hash of the staff member's PIN locally
     */
    fun saveStaffPin(context: Context, staffId: String, pin: String) {
        val hashed = hashPin(pin)
        getPrefs(context).edit().putString("pin_hash_$staffId", hashed).apply()
    }

    /**
     * Verifies an entered PIN against the locally stored encrypted hash
     */
    fun verifyStaffPin(context: Context, staffId: String, enteredPin: String, defaultPin: String = "1234"): Boolean {
        val storedHash = getPrefs(context).getString("pin_hash_$staffId", null)
        val enteredHash = hashPin(enteredPin)

        return if (storedHash != null) {
            storedHash == enteredHash
        } else {
            // If not yet saved in secure storage, compare with default PIN and save hash
            if (enteredPin == defaultPin) {
                saveStaffPin(context, staffId, enteredPin)
                true
            } else {
                false
            }
        }
    }
}
