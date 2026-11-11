package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBackground)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gaming Header Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(CyberOrange, Color.Transparent)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, CyberOrange, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Esports Logo",
                    tint = CyberOrange,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "FF GENIUS AI",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Elite Gaming Companion & Tactical AI Coach",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        color = DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = DangerRed, fontSize = 13.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it; errorMessage = null },
                label = { Text("Username or Email") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_identifier_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = GamingBorder,
                    focusedLabelColor = CyberCyan
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberOrange) },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("toggle_password_visibility")
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberOrange,
                    unfocusedBorderColor = GamingBorder,
                    focusedLabelColor = CyberOrange
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (!isLoading) {
                        scope.launch {
                            isLoading = true
                            val result = authRepository.login(identifier, password)
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> onLoginSuccess()
                                is AuthResult.Error -> errorMessage = result.message
                            }
                        }
                    }
                })
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text("Forgot Password?", color = CyberCyan, fontSize = 13.sp)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val result = authRepository.login(identifier, password)
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> onLoginSuccess()
                            is AuthResult.Error -> errorMessage = result.message
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberOrange,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIGN IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", color = TextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("register_link_button")
                ) {
                    Text("Create Account", color = CyberOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ffUid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBackground)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "CREATE ACCOUNT",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Join elite players with personalized AI coaching",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        color = DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = DangerRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = null },
                label = { Text("Survivor Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_username_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = ffUid,
                onValueChange = { ffUid = it },
                label = { Text("Free Fire In-Game UID (Optional)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = CyberYellow) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_ffuid_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password (min 6 chars, letters & numbers)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberOrange) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberOrange, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = CyberOrange) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_confirmpass_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberOrange, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it; errorMessage = null },
                label = { Text("Recovery: What is your favorite game?") },
                placeholder = { Text("e.g. Free Fire") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = CyberGreen) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_security_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen, unfocusedBorderColor = GamingBorder)
            )

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val result = authRepository.register(
                            username = username,
                            email = email,
                            password = password,
                            confirmPass = confirmPassword,
                            ffUid = ffUid,
                            securityQuestion = "What is your favorite game?",
                            securityAnswer = securityAnswer
                        )
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> onRegisterSuccess()
                            is AuthResult.Error -> errorMessage = result.message
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.White)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTER ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already registered?", color = TextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("login_link_button")
                ) {
                    Text("Sign In", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingBackground)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "PASSWORD RECOVERY",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Verify your registered email and recovery answer to set a new password",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(visible = message != null) {
                message?.let { msg ->
                    Surface(
                        color = if (isError) DangerRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = if (isError) DangerRed else SuccessGreen,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Account Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_email_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Favorite Game (Recovery Answer)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = CyberGreen) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_answer_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberOrange) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_newpass_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberOrange, unfocusedBorderColor = GamingBorder)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = CyberOrange) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_confirmpass_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberOrange, unfocusedBorderColor = GamingBorder)
            )

            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        isError = true
                        message = "Passwords do not match."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        val res = authRepository.resetPassword(email, securityAnswer, newPassword)
                        isLoading = false
                        res.fold(
                            onSuccess = { successMsg ->
                                isError = false
                                message = successMsg
                                onResetSuccess()
                            },
                            onFailure = { err ->
                                isError = true
                                message = err.localizedMessage ?: "Failed to reset password."
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("forgot_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("RESET PASSWORD", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("forgot_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to Sign In", color = TextSecondary)
            }
        }
    }
}
