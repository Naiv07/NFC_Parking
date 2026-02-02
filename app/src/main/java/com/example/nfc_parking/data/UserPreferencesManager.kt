package com.example.nfc_parking.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        // Keys for storing data
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_ID = stringPreferencesKey("user_id")
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    // 🔐 Save login state
    suspend fun saveLoginState(isLoggedIn: Boolean, email: String = "", userId: String = "") {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            preferences[USER_EMAIL] = email
            preferences[USER_ID] = userId
        }
    }

    // 🔐 Get login state
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    // 📧 Get user email
    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL] ?: ""
    }

    // 🆔 Get user ID
    val userId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }

    // 🌗 Save theme preference
    suspend fun saveThemePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }

    // 🌗 Get theme preference
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME] ?: false // Default to light theme
    }

    // 🚪 Clear all data (logout)
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[USER_EMAIL] = ""
            preferences[USER_ID] = ""
        }
    }
}