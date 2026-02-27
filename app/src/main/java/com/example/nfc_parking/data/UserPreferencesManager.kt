package com.example.nfc_parking.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "nfc_parking_prefs"
        private const val KEY_PERMISSIONS_COMPLETED = "permissions_completed"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_THEME_DARK = "theme_dark"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"  // ✅ Added for login state
    }

    // ===== PERMISSIONS =====

    fun hasCompletedPermissions(): Boolean {
        return sharedPreferences.getBoolean(KEY_PERMISSIONS_COMPLETED, false)
    }

    fun setPermissionsCompleted(completed: Boolean = true) {
        sharedPreferences.edit()
            .putBoolean(KEY_PERMISSIONS_COMPLETED, completed)
            .apply()
    }

    // ===== ONBOARDING =====

    fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean = true) {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
    }

    // ===== LOGIN STATE =====

    /**
     * Save login state
     */
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .apply()
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // ===== USER DATA =====

    fun setUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun setUserEmail(email: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun setUserName(name: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    // ===== THEME =====

    fun saveThemePreference(isDark: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_THEME_DARK, isDark)
            .apply()
    }

    fun getThemePreference(): Boolean {
        return sharedPreferences.getBoolean(KEY_THEME_DARK, false)
    }

    // ===== CLEAR DATA =====

    /**
     * Clear all preferences (complete logout)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Clear user data but keep app preferences (permissions, theme)
     */
    fun clearUserData() {
        sharedPreferences.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
    }

    /**
     * Logout user (clear user data but keep app state)
     */
    fun logout() {
        saveLoginState(false)
        clearUserData()
    }
}