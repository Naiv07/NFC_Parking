package com.example.nfc_parking.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfc_parking.R
import com.example.nfc_parking.data.UserPreferencesManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    val isRegister = mutableStateOf(false)
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    private var googleSignInClient: GoogleSignInClient? = null

    // Initialize Google Sign-In
    fun initializeGoogleSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // ⭐ NEW: Force account picker by signing out first
    fun forceAccountPicker(context: Context, onComplete: () -> Unit) {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            Log.d("AuthViewModel", "Signed out for account picker")
            onComplete()
        } ?: run {
            Log.e("AuthViewModel", "GoogleSignInClient is null")
            onComplete()
        }
    }

    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    // Handle Google Sign-In Result
    fun handleGoogleSignInResult(
        data: Intent?,
        preferencesManager: UserPreferencesManager,
        onAuthSuccess: () -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            Log.d("AuthViewModel", "Google Sign-In Success: ${account?.email}")

            if (account != null) {
                firebaseAuthWithGoogle(account, preferencesManager, onAuthSuccess)
            } else {
                isLoading.value = false
                errorMessage.value = "Failed to get Google account"
                Log.e("AuthViewModel", "Account is null")
            }
        } catch (e: ApiException) {
            isLoading.value = false
            errorMessage.value = "Google sign in failed: ${e.message}"
            Log.e("AuthViewModel", "Google Sign-In Error: Code=${e.statusCode}, Message=${e.message}", e)
        }
    }

    // Authenticate with Firebase using Google
    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        preferencesManager: UserPreferencesManager,
        onAuthSuccess: () -> Unit
    ) {
        val idToken = account.idToken

        if (idToken == null) {
            isLoading.value = false
            errorMessage.value = "Failed to get ID token from Google"
            Log.e("AuthViewModel", "ID Token is null")
            return
        }

        Log.d("AuthViewModel", "Authenticating with Firebase using ID token")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("AuthViewModel", "Firebase Auth Success: ${user?.email}")

                    viewModelScope.launch {
                        preferencesManager.saveLoginState(
                            isLoggedIn = true,
                            email = user?.email ?: "",
                            userId = user?.uid ?: ""
                        )
                    }
                    onAuthSuccess()
                } else {
                    errorMessage.value = task.exception?.message ?: "Authentication failed"
                    Log.e("AuthViewModel", "Firebase Auth Error", task.exception)
                }
            }
    }

    // Update methods
    fun updateEmail(newEmail: String) {
        email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword.value = newConfirmPassword
    }

    // Toggle between login and register
    fun toggleMode() {
        isRegister.value = !isRegister.value
        clearFields()
        errorMessage.value = ""
    }

    // Email/Password Login
    fun login(preferencesManager: UserPreferencesManager, onAuthSuccess: () -> Unit) {
        if (email.value.isEmpty() || password.value.isEmpty()) {
            errorMessage.value = "Please fill in all fields"
            return
        }

        isLoading.value = true
        errorMessage.value = ""

        auth.signInWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    viewModelScope.launch {
                        preferencesManager.saveLoginState(
                            isLoggedIn = true,
                            email = user?.email ?: "",
                            userId = user?.uid ?: ""
                        )
                    }
                    onAuthSuccess()
                } else {
                    errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    // Email/Password Register
    fun register(preferencesManager: UserPreferencesManager, onAuthSuccess: () -> Unit) {
        if (email.value.isEmpty() || password.value.isEmpty() || confirmPassword.value.isEmpty()) {
            errorMessage.value = "Please fill in all fields"
            return
        }

        if (password.value != confirmPassword.value) {
            errorMessage.value = "Passwords do not match"
            return
        }

        if (password.value.length < 6) {
            errorMessage.value = "Password must be at least 6 characters"
            return
        }

        isLoading.value = true
        errorMessage.value = ""

        auth.createUserWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    viewModelScope.launch {
                        preferencesManager.saveLoginState(
                            isLoggedIn = true,
                            email = user?.email ?: "",
                            userId = user?.uid ?: ""
                        )
                    }
                    onAuthSuccess()
                } else {
                    errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }

    // Logout
    fun logout(preferencesManager: UserPreferencesManager) {
        auth.signOut()
        googleSignInClient?.signOut()
        viewModelScope.launch {
            preferencesManager.clearUserData()
        }
        clearFields()
    }

    // Clear fields
    private fun clearFields() {
        email.value = ""
        password.value = ""
        confirmPassword.value = ""
        errorMessage.value = ""
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}