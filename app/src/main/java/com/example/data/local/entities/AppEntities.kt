package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val ffUid: String = "",
    val securityQuestion: String = "What is your favorite game?",
    val securityAnswerHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: Long,
    val displayName: String,
    val ffRank: String = "Heroic",
    val favoriteWeapon: String = "MP40",
    val playStyle: String = "Rusher",
    val headshotRate: Float = 64.5f,
    val kdRatio: Float = 3.8f,
    val avatarEmoji: String = "🔥",
    val bio: String = "Grinding for Grandmaster with FF Genius AI",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sensitivity_profiles")
data class SensitivityProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val profileName: String,
    val phoneModel: String,
    val ramGb: Int,
    val refreshRateHz: Int,
    val fingerSetup: String, // "2 Finger", "3 Finger", "4 Finger"
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hud_layouts")
data class HudLayoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val layoutName: String,
    val fingerSetup: String, // "2 Finger", "3 Finger", "4 Finger"
    val configJson: String,  // JSON serialized button coordinates, sizes, opacities
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "skin_designs")
data class SkinDesignEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val itemType: String, // "Gun Skin" or "Character Outfit"
    val weaponOrCharacter: String, // e.g. "MP40", "AK47", "Chrono"
    val theme: String, // "Cyberpunk", "Fire Demon", "Neon Frost", "Dragon Roar"
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val pattern: String,
    val conceptLore: String,
    val statsConcept: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gameplay_reports")
data class GameplayReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val videoUriOrName: String,
    val overallScore: Int,
    val aimScore: Int,
    val movementScore: Int,
    val positioningScore: Int,
    val glooWallScore: Int,
    val decisionScore: Int,
    val summary: String,
    val strengths: String,
    val mistakes: String,
    val recommendations: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "training_progress")
data class TrainingProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val lessonId: String,
    val category: String,
    val isCompleted: Boolean = false,
    val drillScore: Int = 0,
    val attempts: Int = 0,
    val lastPracticedAt: Long = System.currentTimeMillis()
)
