package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    data object Dashboard : Screen("dashboard")
    data object LiveCoach : Screen("live_coach")
    data object GameplayAnalyzer : Screen("gameplay_analyzer")
    data object SensitivityGenerator : Screen("sensitivity_generator")
    data object HudDesigner : Screen("hud_designer")
    data object SkinDesigner : Screen("skin_designer")
    data object Preview3D : Screen("preview_3d")
    data object PerformanceCenter : Screen("performance_center")
    data object TrainingCenter : Screen("training_center")
    data object AiChatbot : Screen("ai_chatbot")
    data object MyReports : Screen("my_reports")
    data object Settings : Screen("settings")
}
