package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.daos.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        UserProfileEntity::class,
        SensitivityProfileEntity::class,
        HudLayoutEntity::class,
        SkinDesignEntity::class,
        GameplayReportEntity::class,
        TrainingProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun sensitivityDao(): SensitivityDao
    abstract fun hudDao(): HudDao
    abstract fun skinDao(): SkinDao
    abstract fun reportDao(): ReportDao
    abstract fun trainingDao(): TrainingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ff_genius_ai_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
