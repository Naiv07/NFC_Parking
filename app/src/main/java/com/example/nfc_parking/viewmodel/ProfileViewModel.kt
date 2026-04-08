package com.example.nfc_parking.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfc_parking.data.UserPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val userName = mutableStateOf("")
    val userEmail = mutableStateOf("")
    val userId = mutableStateOf("")

    /**
     * Load user data from preferences
     */
    fun loadUserData(preferencesManager: UserPreferencesManager) {
        viewModelScope.launch {
            // Get email and userId from UserPreferencesManager
            userEmail.value = preferencesManager.getUserEmail() ?: ""
            userId.value = preferencesManager.getUserId() ?: ""

            // Get display name from Firebase Auth if available
            auth.currentUser?.let { user ->
                userName.value = user.displayName ?: extractNameFromEmail(userEmail.value)
            } ?: run {
                userName.value = extractNameFromEmail(userEmail.value)
            }
        }
    }

    /**
     * Extract name from email (e.g., "esther84@gmail.com" -> "Esther Howard")
     */
    private fun extractNameFromEmail(email: String): String {
        if (email.isEmpty()) return "User"

        val username = email.substringBefore("@")
        // Remove numbers and split by common separators
        val parts = username.replace(Regex("[0-9]"), "")
            .split(Regex("[._-]"))
            .filter { it.isNotEmpty() }

        return parts.joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercase() }
        }
    }

    /**
     * Logout function
     */
    fun logout(preferencesManager: UserPreferencesManager, onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            // Sign out from Firebase
            auth.signOut()

            // Clear user preferences
            preferencesManager.clearUserData()

            // Navigate to auth screen
            onLogoutSuccess()
        }
    }
}