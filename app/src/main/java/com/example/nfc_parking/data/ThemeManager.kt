package com.example.nfc_parking.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ThemeManager {
    val isDarkTheme = mutableStateOf(false)

    private var preferencesManager: UserPreferencesManager? = null

    // Initialize with context
    fun initialize(context: Context) {
        preferencesManager = UserPreferencesManager(context)

        // Load saved theme preference
        CoroutineScope(Dispatchers.IO).launch {
            val savedTheme = preferencesManager?.isDarkTheme?.first() ?: false
            isDarkTheme.value = savedTheme
        }
    }

    // Toggle theme (no parameter needed)
    fun toggleTheme(bool: Boolean) {
        val newValue = !isDarkTheme.value
        isDarkTheme.value = newValue

        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager?.saveThemePreference(newValue)
        }
    }

    // Set specific theme value
    fun setTheme(newValue: Boolean) {
        isDarkTheme.value = newValue

        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager?.saveThemePreference(newValue)
        }
    }
}