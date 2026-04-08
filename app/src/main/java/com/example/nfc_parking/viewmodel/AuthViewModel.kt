package com.example.nfc_parking.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfc_parking.data.UserPreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var googleSignInEnabled = false

    // UI State
    val isRegister = mutableStateOf(false)
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    // Forgot Password State
    val resetEmailSent = mutableStateOf(false)
    val resetEmailError = mutableStateOf("")

    /**
     * Initialize Google Sign In (safe - won't crash if not configured)
     */
    fun initializeGoogleSignIn(context: Context) {
        try {
            // Web Client ID from google-services.json (client_type: 3)
            val webClientId = "535901221192-bu5g51cms1duhhf6pqq12h5aes8g9194.apps.googleusercontent.com"

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInEnabled = true
            android.util.Log.d("AuthViewModel", "Google Sign-In initialized successfully")

        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Google Sign-In initialization failed: ${e.message}", e)
            googleSignInEnabled = false
        }
    }

    /**
     * Get Google Sign In Intent
     */
    fun getGoogleSignInIntent(): Intent? {
        return try {
            if (!googleSignInEnabled) {
                errorMessage.value = "Google Sign-In not configured"
                return null
            }
            googleSignInClient?.signInIntent
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Failed to get sign-in intent", e)
            null
        }
    }

    /**
     * Force account picker to show
     */
    fun forceAccountPicker(context: Context, onComplete: () -> Unit) {
        try {
            googleSignInClient?.signOut()?.addOnCompleteListener {
                onComplete()
            } ?: run {
                android.util.Log.w("AuthViewModel", "Google Sign-In client is null")
                onComplete()
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Force account picker failed", e)
            onComplete()
        }
    }

    /**
     * Handle Google Sign In Result
     */
    fun handleGoogleSignInResult(
        data: Intent?,
        preferencesManager: UserPreferencesManager,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "handleGoogleSignInResult called")

                if (!googleSignInEnabled) {
                    android.util.Log.e("AuthViewModel", "Google Sign-In not enabled")
                    isLoading.value = false
                    errorMessage.value = "Google Sign-In not available"
                    return@launch
                }

                if (data == null) {
                    android.util.Log.e("AuthViewModel", "Intent data is null")
                    isLoading.value = false
                    errorMessage.value = "Sign-in was cancelled"
                    return@launch
                }

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                android.util.Log.d("AuthViewModel", "Getting account from intent...")

                val account = task.getResult(ApiException::class.java)
                android.util.Log.d("AuthViewModel", "Account retrieved: ${account?.email}")

                if (account != null) {
                    if (account.idToken == null) {
                        android.util.Log.e("AuthViewModel", "ID Token is null - check SHA-1 certificate in Firebase Console")
                        isLoading.value = false
                        errorMessage.value = "Authentication failed. Please check app configuration."
                        return@launch
                    }

                    android.util.Log.d("AuthViewModel", "Creating Firebase credential...")
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    android.util.Log.d("AuthViewModel", "Signing in with Firebase...")
                    val result = auth.signInWithCredential(credential).await()
                    val user = result.user

                    if (user != null) {
                        android.util.Log.d("AuthViewModel", "Firebase sign-in successful: ${user.email}")
                        preferencesManager.saveLoginState(true)
                        preferencesManager.setUserId(user.uid)
                        preferencesManager.setUserEmail(user.email ?: "")
                        preferencesManager.setUserName(user.displayName ?: "")

                        isLoading.value = false
                        errorMessage.value = ""
                        onSuccess()
                    } else {
                        android.util.Log.e("AuthViewModel", "Firebase user is null after sign-in")
                        isLoading.value = false
                        errorMessage.value = "Authentication failed"
                    }
                } else {
                    android.util.Log.e("AuthViewModel", "Google account is null")
                    isLoading.value = false
                    errorMessage.value = "Failed to get account information"
                }
            } catch (e: ApiException) {
                val errorCode = e.statusCode
                android.util.Log.e("AuthViewModel", "Google sign in failed with status code: $errorCode", e)
                isLoading.value = false
                errorMessage.value = when (errorCode) {
                    10 -> "Developer error: Check SHA-1 certificate in Firebase Console"
                    12500 -> "Sign-in cancelled"
                    12501 -> "Sign-in attempt cancelled"
                    else -> "Google Sign-In error: $errorCode"
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Auth error: ${e.message}", e)
                isLoading.value = false
                errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

    /**
     * Send Password Reset Email
     */
    fun sendPasswordResetEmail(emailAddress: String) {
        if (emailAddress.isBlank()) {
            resetEmailError.value = "Please enter your email address"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            resetEmailError.value = "Invalid email format"
            return
        }

        isLoading.value = true
        resetEmailError.value = ""
        resetEmailSent.value = false

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(emailAddress.trim()).await()
                isLoading.value = false
                resetEmailSent.value = true
                android.util.Log.d("AuthViewModel", "Password reset email sent to $emailAddress")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Password reset failed: ${e.message}", e)
                isLoading.value = false
                resetEmailError.value = when {
                    e.message?.contains("user-not-found") == true -> "No account found with this email"
                    e.message?.contains("network") == true -> "Network error. Please try again."
                    else -> "Failed to send reset email. Try again."
                }
            }
        }
    }

    fun clearResetState() {
        resetEmailSent.value = false
        resetEmailError.value = ""
    }

    /**
     * Email/Password Login (Always works)
     */
    fun login(preferencesManager: UserPreferencesManager, onSuccess: () -> Unit) {
        if (!validateLogin()) return

        isLoading.value = true
        errorMessage.value = ""

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(
                    email.value.trim(),
                    password.value
                ).await()

                val user = result.user
                if (user != null) {
                    preferencesManager.saveLoginState(true)
                    preferencesManager.setUserId(user.uid)
                    preferencesManager.setUserEmail(user.email ?: "")
                    preferencesManager.setUserName(user.displayName ?: "")

                    isLoading.value = false
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error: ${e.message}", e)
                isLoading.value = false
                errorMessage.value = when {
                    e.message?.contains("password") == true -> "Invalid email or password"
                    e.message?.contains("user-not-found") == true -> "No account found with this email"
                    e.message?.contains("network") == true -> "Network error"
                    else -> "Login failed: ${e.message}"
                }
            }
        }
    }

    /**
     * Email/Password Registration (Always works)
     */
    fun register(preferencesManager: UserPreferencesManager, onSuccess: () -> Unit) {
        if (!validateRegistration()) return

        isLoading.value = true
        errorMessage.value = ""

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(
                    email.value.trim(),
                    password.value
                ).await()

                val user = result.user
                if (user != null) {
                    preferencesManager.saveLoginState(true)
                    preferencesManager.setUserId(user.uid)
                    preferencesManager.setUserEmail(email.value)
                    preferencesManager.setUserName("")

                    isLoading.value = false
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Registration error: ${e.message}", e)
                isLoading.value = false
                errorMessage.value = when {
                    e.message?.contains("already in use") == true -> "Email already registered"
                    e.message?.contains("weak-password") == true -> "Password too weak (min 6 characters)"
                    e.message?.contains("invalid-email") == true -> "Invalid email format"
                    e.message?.contains("network") == true -> "Network error"
                    else -> "Registration failed: ${e.message}"
                }
            }
        }
    }

    private fun validateLogin(): Boolean {
        return when {
            email.value.isBlank() -> {
                errorMessage.value = "Email is required"
                false
            }
            password.value.isBlank() -> {
                errorMessage.value = "Password is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches() -> {
                errorMessage.value = "Invalid email format"
                false
            }
            else -> true
        }
    }

    private fun validateRegistration(): Boolean {
        return when {
            email.value.isBlank() -> {
                errorMessage.value = "Email is required"
                false
            }
            password.value.isBlank() -> {
                errorMessage.value = "Password is required"
                false
            }
            confirmPassword.value.isBlank() -> {
                errorMessage.value = "Please confirm your password"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches() -> {
                errorMessage.value = "Invalid email format"
                false
            }
            password.value.length < 6 -> {
                errorMessage.value = "Password must be at least 6 characters"
                false
            }
            password.value != confirmPassword.value -> {
                errorMessage.value = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    fun updateEmail(newEmail: String) {
        email.value = newEmail
        errorMessage.value = ""
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
        errorMessage.value = ""
    }

    fun updateConfirmPassword(newPassword: String) {
        confirmPassword.value = newPassword
        errorMessage.value = ""
    }

    fun toggleMode() {
        isRegister.value = !isRegister.value
        errorMessage.value = ""
        password.value = ""
        confirmPassword.value = ""
    }

    fun signOut(preferencesManager: UserPreferencesManager, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.signOut()
                googleSignInClient?.signOut()
                preferencesManager.logout()

                email.value = ""
                password.value = ""
                confirmPassword.value = ""
                errorMessage.value = ""

                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Sign out error", e)
            }
        }
    }
}