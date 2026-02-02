package com.example.nfc_parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.UserPreferencesManager
import com.example.nfc_parking.navigation.AppNavGraph
import com.example.nfc_parking.navigation.NavRoutes
import com.example.nfc_parking.ui.theme.NFC_parkingTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize preferences manager
        preferencesManager = UserPreferencesManager(this)

        // Initialize ThemeManager
        ThemeManager.initialize(this)

        // Check if user is logged in
        val isLoggedIn = runBlocking {
            preferencesManager.isLoggedIn.first()
        }

        setContent {
            val isDarkTheme by ThemeManager.isDarkTheme

            NFC_parkingTheme(
                darkTheme = isDarkTheme
            ) {
                val navController = rememberNavController()

                // Set start destination based on login state
                AppNavGraph(
                    navController = navController,
                    startDestination = if (isLoggedIn) NavRoutes.HOME else NavRoutes.AUTH,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
}