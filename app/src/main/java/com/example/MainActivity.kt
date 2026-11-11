package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.api.GeminiApiService
import com.example.data.auth.SessionManager
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.repository.AuthRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.FFGeniusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val sessionManager = SessionManager(this)
        val authRepository = AuthRepository(db, sessionManager)
        val appRepository = AppRepository(db, sessionManager)
        val apiService = GeminiApiService(this)

        setContent {
            FFGeniusTheme {
                AppNavigation(
                    authRepository = authRepository,
                    appRepository = appRepository,
                    sessionManager = sessionManager,
                    apiService = apiService
                )
            }
        }
    }
}
