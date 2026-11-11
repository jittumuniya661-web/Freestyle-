package com.example.data.local.daos

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt WHERE email = :email")
    suspend fun updatePassword(email: String, newHash: String, newSalt: String): Int

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
}

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getProfileFlow(userId: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfile(userId: Long): UserProfileEntity?
}

@Dao
interface SensitivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SensitivityProfileEntity): Long

    @Update
    suspend fun update(profile: SensitivityProfileEntity)

    @Delete
    suspend fun delete(profile: SensitivityProfileEntity)

    @Query("SELECT * FROM sensitivity_profiles WHERE userId = :userId ORDER BY createdAt DESC")
    fun getProfilesForUser(userId: Long): Flow<List<SensitivityProfileEntity>>

    @Query("SELECT * FROM sensitivity_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): SensitivityProfileEntity?
}

@Dao
interface HudDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(layout: HudLayoutEntity): Long

    @Update
    suspend fun update(layout: HudLayoutEntity)

    @Delete
    suspend fun delete(layout: HudLayoutEntity)

    @Query("SELECT * FROM hud_layouts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLayoutsForUser(userId: Long): Flow<List<HudLayoutEntity>>

    @Query("SELECT * FROM hud_layouts WHERE id = :id LIMIT 1")
    suspend fun getLayoutById(id: Long): HudLayoutEntity?
}

@Dao
interface SkinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skin: SkinDesignEntity): Long

    @Delete
    suspend fun delete(skin: SkinDesignEntity)

    @Query("SELECT * FROM skin_designs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSkinsForUser(userId: Long): Flow<List<SkinDesignEntity>>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: GameplayReportEntity): Long

    @Query("SELECT * FROM gameplay_reports WHERE userId = :userId ORDER BY createdAt DESC")
    fun getReportsForUser(userId: Long): Flow<List<GameplayReportEntity>>

    @Query("SELECT * FROM gameplay_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): GameplayReportEntity?

    @Delete
    suspend fun delete(report: GameplayReportEntity)
}

@Dao
interface TrainingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: TrainingProgressEntity)

    @Query("SELECT * FROM training_progress WHERE userId = :userId")
    fun getProgressForUser(userId: Long): Flow<List<TrainingProgressEntity>>

    @Query("SELECT * FROM training_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getLessonProgress(userId: Long, lessonId: String): TrainingProgressEntity?
}
