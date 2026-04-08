package com.example.nfc_parking.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeManager {
    val isDarkTheme = mutableStateOf(false)

    private var preferencesManager: UserPreferencesManager? = null

    /**
     * Initialize with context
     */
    fun initialize(context: Context) {
        preferencesManager = UserPreferencesManager(context)

        // Load saved theme preference
        CoroutineScope(Dispatchers.IO).launch {
            val savedTheme = preferencesManager?.getThemePreference() ?: false
            isDarkTheme.value = savedTheme
        }
    }

    /**
     * Toggle theme (accepts boolean parameter to set specific value)
     */
    fun toggleTheme(newValue: Boolean) {
        isDarkTheme.value = newValue

        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager?.saveThemePreference(newValue)
        }
    }

    /**
     * Set specific theme value (RECOMMENDED - prevents auto-toggle)
     */
    fun setTheme(newValue: Boolean) {
        isDarkTheme.value = newValue

        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager?.saveThemePreference(newValue)
        }
    }
}