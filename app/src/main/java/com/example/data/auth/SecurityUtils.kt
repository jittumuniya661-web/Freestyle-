package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object SecurityUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated.equals(expectedHash, ignoreCase = true)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && emailRegex.matches(email.trim())
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) return "Password must be at least 6 characters."
        if (password.none { it.isDigit() }) return "Password must contain at least one number."
        if (password.none { it.isLetter() }) return "Password must contain at least one letter."
        return null
    }
}

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ff_genius_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USERNAME = "cached_username"
        private const val KEY_EMAIL = "cached_email"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_PREFERRED_LANG = "preferred_language" // "en" or "hi"
    }

    fun saveSession(userId: Long, username: String, email: String): String {
        val token = "ff_tok_" + UUID.randomUUID().toString().replace("-", "")
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
        return token
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() > 0 && !getAuthToken().isNullOrBlank()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "Survivor") ?: "Survivor"
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getCustomApiKey(): String = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_PREFERRED_LANG, lang).apply()
    }

    fun getLanguage(): String = prefs.getString(KEY_PREFERRED_LANG, "en") ?: "en"
}
