package com.example.nfc_parking.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.UserPreferencesManager
import com.example.nfc_parking.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    preferencesManager: UserPreferencesManager,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isRegister by viewModel.isRegister
    val email by viewModel.email
    val password by viewModel.password
    val confirmPassword by viewModel.confirmPassword
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val isDarkTheme by ThemeManager.isDarkTheme
    val resetEmailSent by viewModel.resetEmailSent
    val resetEmailError by viewModel.resetEmailError

    // Forgot Password Dialog State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // Initialize Google Sign-In
    LaunchedEffect(Unit) {
        viewModel.initializeGoogleSignIn(context)
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotPasswordEmail = ""
                viewModel.clearResetState()
            },
            title = {
                Text(
                    text = "Reset Password",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1F2937)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your email address and we'll send you a link to reset your password.",
                        fontSize = 14.sp,
                        color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = {
                            forgotPasswordEmail = it
                            viewModel.clearResetState()
                        },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )
                    if (resetEmailError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetEmailError,
                            color = Color(0xFFFF4444),
                            fontSize = 13.sp
                        )
                    }
                    if (resetEmailSent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ Reset link sent! Check your inbox.",
                            color = Color(0xFF4CAF50),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmailSent) {
                            showForgotPasswordDialog = false
                            forgotPasswordEmail = ""
                            viewModel.clearResetState()
                        } else {
                            viewModel.sendPasswordResetEmail(forgotPasswordEmail)
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = if (isDarkTheme) Color.Black else Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (resetEmailSent) "Done" else "Send Reset Link",
                            color = if (isDarkTheme) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                if (!resetEmailSent) {
                    TextButton(
                        onClick = {
                            showForgotPasswordDialog = false
                            forgotPasswordEmail = ""
                            viewModel.clearResetState()
                        }
                    ) {
                        Text(
                            text = "Cancel",
                            color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
        )
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data, preferencesManager, onAuthSuccess)
        } else {
            // Reset loading state if sign-in was cancelled
            viewModel.isLoading.value = false
        }
    }

    // 🎨 Color Scheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF0F0F0F),
                        Color(0xFF121212)
                    ) else listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE8F4F8),
                        Color(0xFFDCE9F0)
                    )
                )
            )
    ) {
        // 🌗 THEME TOGGLE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable {
                        ThemeManager.toggleTheme(!isDarkTheme)
                    },
                color = cardColor,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 🅿️ Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🧾 TITLE
            Text(
                text = if (isRegister) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isRegister)
                    "Sign up to get started with NFC Parking"
                else
                    "Login to continue your parking journey",
                color = subtextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🔵 GOOGLE SIGN-IN BUTTON
            OutlinedButton(
                onClick = {
                    if (!isLoading) {
                        viewModel.isLoading.value = true
                        // ⭐ FORCE SIGN OUT FIRST TO SHOW ACCOUNT PICKER
                        viewModel.forceAccountPicker(context) {
                            val signInIntent = viewModel.getGoogleSignInIntent()
                            if (signInIntent != null) {
                                googleSignInLauncher.launch(signInIntent)
                            } else {
                                viewModel.isLoading.value = false
                                viewModel.errorMessage.value = "Failed to initialize Google Sign-In"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = cardColor,
                    contentColor = textColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, subtextColor)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ➖ OR DIVIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = subtextColor.copy(alpha = 0.3f)
                )
                Text(
                    text = "  OR  ",
                    color = subtextColor,
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = subtextColor.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ⚠️ ERROR MESSAGE
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF4444).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFFF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF4444),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 📧 EMAIL FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email", color = subtextColor) },
                placeholder = { Text("Enter your email", color = subtextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = accentColor
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = accentColor,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔑 PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password", color = subtextColor) },
                placeholder = { Text("Enter your password", color = subtextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = accentColor
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = subtextColor
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = accentColor,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor
                ),
                singleLine = true
            )

            // 🔁 CONFIRM PASSWORD (REGISTER ONLY)
            if (isRegister) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    label = { Text("Confirm Password", color = subtextColor) },
                    placeholder = { Text("Re-enter your password", color = subtextColor.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = subtextColor
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor,
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ▶️ ACTION BUTTON
            Button(
                onClick = {
                    if (isRegister) {
                        viewModel.register(preferencesManager, onAuthSuccess)
                    } else {
                        viewModel.login(preferencesManager, onAuthSuccess)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (isDarkTheme) Color.Black else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegister) "Create Account" else "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.Black else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔄 TOGGLE LOGIN / REGISTER
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegister)
                        "Already have an account? "
                    else
                        "Don't have an account? ",
                    color = subtextColor,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isRegister) "Login" else "Sign Up",
                    modifier = Modifier.clickable {
                        if (!isLoading) {
                            viewModel.toggleMode()
                        }
                    },
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password
            if (!isRegister) {
                Text(
                    text = "Forgot Password?",
                    modifier = Modifier.clickable {
                        if (!isLoading) {
                            forgotPasswordEmail = email
                            viewModel.clearResetState()
                            showForgotPasswordDialog = true
                        }
                    },
                    color = accentColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}