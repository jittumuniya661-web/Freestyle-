package com.example.data.repository

import com.example.data.auth.SecurityUtils
import com.example.data.auth.SessionManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class AuthResult {
    data class Success(val user: UserEntity, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) {
    private val userDao = db.userDao()
    private val profileDao = db.userProfileDao()

    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPass: String,
        ffUid: String,
        securityQuestion: String,
        securityAnswer: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanUser = username.trim()

        if (cleanUser.length < 3) {
            return@withContext AuthResult.Error("Username must be at least 3 characters.")
        }
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        val passValidation = SecurityUtils.validatePassword(password)
        if (passValidation != null) {
            return@withContext AuthResult.Error(passValidation)
        }
        if (password != confirmPass) {
            return@withContext AuthResult.Error("Passwords do not match.")
        }
        if (securityAnswer.isBlank()) {
            return@withContext AuthResult.Error("Please provide an answer for password recovery.")
        }

        // Check duplicates
        val existingEmail = userDao.getUserByEmail(cleanEmail)
        if (existingEmail != null) {
            return@withContext AuthResult.Error("An account with this email already exists.")
        }
        val existingUser = userDao.getUserByUsername(cleanUser)
        if (existingUser != null) {
            return@withContext AuthResult.Error("This username is already taken. Please pick another.")
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val answerSalt = SecurityUtils.generateSalt()
        val answerHash = SecurityUtils.hashPassword(securityAnswer.trim().lowercase(), answerSalt)

        val newUser = UserEntity(
            username = cleanUser,
            email = cleanEmail,
            passwordHash = passwordHash,
            salt = salt,
            ffUid = ffUid.trim(),
            securityQuestion = securityQuestion,
            securityAnswerHash = "$answerSalt:$answerHash"
        )

        try {
            val userId = userDao.insertUser(newUser)
            val createdUser = newUser.copy(id = userId)

            // Seed default profile
            profileDao.upsertProfile(
                UserProfileEntity(
                    userId = userId,
                    displayName = cleanUser,
                    ffRank = "Heroic",
                    favoriteWeapon = "MP40",
                    playStyle = "Rusher",
                    headshotRate = 62.0f,
                    kdRatio = 3.6f
                )
            )

            // Seed sample sensitivity profile and HUD layout for new user
            seedDefaultData(userId)

            val token = sessionManager.saveSession(userId, cleanUser, cleanEmail)
            AuthResult.Success(createdUser, token)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed.")
        }
    }

    suspend fun login(identifier: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        if (cleanId.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter both username/email and password.")
        }

        val user = if (cleanId.contains("@")) {
            userDao.getUserByEmail(cleanId.lowercase())
        } else {
            userDao.getUserByUsername(cleanId)
        }

        if (user == null) {
            return@withContext AuthResult.Error("No user found with the provided credentials.")
        }

        val passwordMatches = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!passwordMatches) {
            return@withContext AuthResult.Error("Incorrect password. Please try again.")
        }

        val token = sessionManager.saveSession(user.id, user.username, user.email)
        AuthResult.Success(user, token)
    }

    suspend fun resetPassword(
        email: String,
        securityAnswer: String,
        newPassword: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(Exception("No account found with this email."))

        val parts = user.securityAnswerHash.split(":")
        if (parts.size == 2) {
            val salt = parts[0]
            val expectedHash = parts[1]
            if (!SecurityUtils.verifyPassword(securityAnswer.trim().lowercase(), salt, expectedHash)) {
                return@withContext Result.failure(Exception("Security answer is incorrect."))
            }
        }

        val passValidation = SecurityUtils.validatePassword(newPassword)
        if (passValidation != null) {
            return@withContext Result.failure(Exception(passValidation))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        userDao.updatePassword(cleanEmail, newHash, newSalt)
        Result.success("Password reset successfully. You can now log in.")
    }

    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        val userId = sessionManager.getUserId()
        if (userId <= 0) return@withContext null
        userDao.getUserById(userId)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    private suspend fun seedDefaultData(userId: Long) {
        // Seed default Pro sensitivity profile
        db.sensitivityDao().insert(
            SensitivityProfileEntity(
                userId = userId,
                profileName = "Pro Rusher (Drag Headshot)",
                phoneModel = "Universal Flagship",
                ramGb = 8,
                refreshRateHz = 120,
                fingerSetup = "3 Finger",
                general = 98,
                redDot = 95,
                scope2x = 92,
                scope4x = 88,
                sniperScope = 65,
                freeLook = 80,
                isFavorite = true
            )
        )
        // Seed default 3-finger HUD layout
        val initialHudConfig = """[
            {"id":"fire_right","name":"Right Fire","x":0.82,"y":0.72,"size":62,"opacity":85},
            {"id":"fire_left","name":"Left Fire","x":0.16,"y":0.35,"size":55,"opacity":80},
            {"id":"gloo_wall","name":"Gloo Wall","x":0.22,"y":0.68,"size":70,"opacity":95},
            {"id":"jump","name":"Jump","x":0.90,"y":0.48,"size":58,"opacity":85},
            {"id":"crouch","name":"Crouch","x":0.78,"y":0.84,"size":56,"opacity":85},
            {"id":"scope","name":"Scope","x":0.72,"y":0.45,"size":60,"opacity":90},
            {"id":"reload","name":"Reload","x":0.65,"y":0.28,"size":50,"opacity":75},
            {"id":"joystick","name":"Movement","x":0.18,"y":0.75,"size":65,"opacity":70}
        ]"""
        db.hudDao().insert(
            HudLayoutEntity(
                userId = userId,
                layoutName = "Pro 3-Finger Competitive",
                fingerSetup = "3 Finger",
                configJson = initialHudConfig,
                isDefault = true
            )
        )
    }
}
