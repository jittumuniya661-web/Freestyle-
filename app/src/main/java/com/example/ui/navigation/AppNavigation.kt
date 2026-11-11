package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.api.GeminiApiService
import com.example.data.auth.SessionManager
import com.example.data.repository.AppRepository
import com.example.data.repository.AuthRepository
import com.example.ui.screens.analyzer.GameplayAnalyzerScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.chatbot.AiChatbotScreen
import com.example.ui.screens.coach.LiveCoachScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.hud.HudDesignerScreen
import com.example.ui.screens.performance.PerformanceCenterScreen
import com.example.ui.screens.preview3d.Preview3DScreen
import com.example.ui.screens.reports.MyReportsScreen
import com.example.ui.screens.sensitivity.SensitivityGeneratorScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.skin.SkinDesignerScreen
import com.example.ui.screens.training.TrainingCenterScreen
import com.example.ui.theme.GamingBackground

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    appRepository: AppRepository,
    sessionManager: SessionManager,
    apiService: GeminiApiService,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (sessionManager.isLoggedIn()) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Authentication
            composable(Screen.Login.route) {
                LoginScreen(
                    authRepository = authRepository,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    authRepository = authRepository,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authRepository = authRepository,
                    onResetSuccess = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    appRepository = appRepository,
                    sessionManager = sessionManager,
                    apiService = apiService,
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }

            // AI Live Coach
            composable(Screen.LiveCoach.route) {
                LiveCoachScreen(
                    apiService = apiService,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Gameplay Analyzer
            composable(Screen.GameplayAnalyzer.route) {
                GameplayAnalyzerScreen(
                    appRepository = appRepository,
                    apiService = apiService,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sensitivity Generator
            composable(Screen.SensitivityGenerator.route) {
                SensitivityGeneratorScreen(
                    appRepository = appRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Custom HUD Designer
            composable(Screen.HudDesigner.route) {
                HudDesignerScreen(
                    appRepository = appRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // AI Skin Designer
            composable(Screen.SkinDesigner.route) {
                SkinDesignerScreen(
                    appRepository = appRepository,
                    apiService = apiService,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 3D Preview
            composable(Screen.Preview3D.route) {
                Preview3DScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Gaming Performance
            composable(Screen.PerformanceCenter.route) {
                PerformanceCenterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // AI Training Center
            composable(Screen.TrainingCenter.route) {
                TrainingCenterScreen(
                    appRepository = appRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // AI Gaming Chatbot
            composable(Screen.AiChatbot.route) {
                AiChatbotScreen(
                    apiService = apiService,
                    sessionManager = sessionManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // My Reports
            composable(Screen.MyReports.route) {
                MyReportsScreen(
                    appRepository = appRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAnalyzer = {
                        navController.navigate(Screen.GameplayAnalyzer.route)
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    authRepository = authRepository,
                    appRepository = appRepository,
                    sessionManager = sessionManager,
                    apiService = apiService,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
