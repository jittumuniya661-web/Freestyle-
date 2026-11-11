package com.example.data.repository

import com.example.data.auth.SessionManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) {
    private val profileDao = db.userProfileDao()
    private val sensitivityDao = db.sensitivityDao()
    private val hudDao = db.hudDao()
    private val skinDao = db.skinDao()
    private val reportDao = db.reportDao()
    private val trainingDao = db.trainingDao()

    val currentUserId: Long get() = sessionManager.getUserId()

    // --- Profile ---
    fun getProfileFlow(): Flow<UserProfileEntity?> = profileDao.getProfileFlow(currentUserId)
    suspend fun getProfile(): UserProfileEntity? = profileDao.getProfile(currentUserId)
    suspend fun saveProfile(profile: UserProfileEntity) = profileDao.upsertProfile(profile)

    // --- Sensitivity ---
    fun getSensitivityProfiles(): Flow<List<SensitivityProfileEntity>> =
        sensitivityDao.getProfilesForUser(currentUserId)

    suspend fun saveSensitivityProfile(profile: SensitivityProfileEntity): Long =
        sensitivityDao.insert(profile.copy(userId = currentUserId))

    suspend fun updateSensitivityProfile(profile: SensitivityProfileEntity) =
        sensitivityDao.update(profile)

    suspend fun deleteSensitivityProfile(profile: SensitivityProfileEntity) =
        sensitivityDao.delete(profile)

    // --- HUD Layouts ---
    fun getHudLayouts(): Flow<List<HudLayoutEntity>> =
        hudDao.getLayoutsForUser(currentUserId)

    suspend fun saveHudLayout(layout: HudLayoutEntity): Long =
        hudDao.insert(layout.copy(userId = currentUserId))

    suspend fun updateHudLayout(layout: HudLayoutEntity) =
        hudDao.update(layout)

    suspend fun deleteHudLayout(layout: HudLayoutEntity) =
        hudDao.delete(layout)

    // --- Skin Designs ---
    fun getSkinDesigns(): Flow<List<SkinDesignEntity>> =
        skinDao.getSkinsForUser(currentUserId)

    suspend fun saveSkinDesign(skin: SkinDesignEntity): Long =
        skinDao.insert(skin.copy(userId = currentUserId))

    suspend fun deleteSkinDesign(skin: SkinDesignEntity) =
        skinDao.delete(skin)

    // --- Gameplay Reports ---
    fun getReports(): Flow<List<GameplayReportEntity>> =
        reportDao.getReportsForUser(currentUserId)

    suspend fun saveReport(report: GameplayReportEntity): Long =
        reportDao.insert(report.copy(userId = currentUserId))

    suspend fun getReportById(id: Long): GameplayReportEntity? =
        reportDao.getReportById(id)

    suspend fun deleteReport(report: GameplayReportEntity) =
        reportDao.delete(report)

    // --- Training Progress ---
    fun getTrainingProgress(): Flow<List<TrainingProgressEntity>> =
        trainingDao.getProgressForUser(currentUserId)

    suspend fun updateTrainingProgress(lessonId: String, category: String, completed: Boolean, score: Int) =
        withContext(Dispatchers.IO) {
            val existing = trainingDao.getLessonProgress(currentUserId, lessonId)
            val updated = existing?.copy(
                isCompleted = completed || existing.isCompleted,
                drillScore = maxOf(score, existing.drillScore),
                attempts = existing.attempts + 1,
                lastPracticedAt = System.currentTimeMillis()
            ) ?: TrainingProgressEntity(
                userId = currentUserId,
                lessonId = lessonId,
                category = category,
                isCompleted = completed,
                drillScore = score,
                attempts = 1,
                lastPracticedAt = System.currentTimeMillis()
            )
            trainingDao.upsert(updated)
        }

    // --- Database Backup (JSON Export) ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("userId", currentUserId)
        root.put("exportedAt", System.currentTimeMillis())

        // Profile
        val profile = profileDao.getProfile(currentUserId)
        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("displayName", profile.displayName)
            pObj.put("ffRank", profile.ffRank)
            pObj.put("favoriteWeapon", profile.favoriteWeapon)
            pObj.put("playStyle", profile.playStyle)
            pObj.put("headshotRate", profile.headshotRate)
            pObj.put("kdRatio", profile.kdRatio)
            root.put("profile", pObj)
        }

        // Sensitivity
        val sensList = sensitivityDao.getProfileById(currentUserId) // or we can export single or multiple
        root.put("appVersion", "1.0.0-PRO")
        root.toString(2)
    }
}
