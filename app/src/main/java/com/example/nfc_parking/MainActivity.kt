package com.example.nfc_parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.nfc_parking.data.AlertManager
import com.example.nfc_parking.data.BookingHistory
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.UserPreferencesManager
import com.example.nfc_parking.navigation.AppNavGraph
import com.example.nfc_parking.navigation.NavRoutes
import com.example.nfc_parking.ui.theme.NFC_parkingTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize BookingHistory persistence
        BookingHistory.initialize(applicationContext)

        // ✅ Initialize time-based alerts monitoring
        AlertManager.initializeMonitoring()

        preferencesManager = UserPreferencesManager(this)
        ThemeManager.initialize(this)

        AlertManager.initializeMonitoring()

        // ✅ Use lifecycleScope instead of runBlocking
        var initialRoute = NavRoutes.AUTH

        lifecycleScope.launch {
            val isLoggedIn = preferencesManager.isLoggedIn.first()
            initialRoute = if (isLoggedIn) NavRoutes.HOME else NavRoutes.AUTH
        }

        setContent {
            val isDarkTheme by ThemeManager.isDarkTheme

            NFC_parkingTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    startDestination = initialRoute,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // ✅ Cleanup AlertManager when app closes
        AlertManager.cleanup()
    }
}