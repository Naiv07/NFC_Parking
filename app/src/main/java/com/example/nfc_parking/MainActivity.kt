package com.example.nfc_parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nfc_parking.data.AlertsManager
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.UserPreferencesManager
import com.example.nfc_parking.data.VehicleManager
import com.example.nfc_parking.navigation.AppNavGraph
import com.example.nfc_parking.navigation.NavRoutes
import com.example.nfc_parking.ui.theme.Nfc_parkingTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize AlertsManager with application context
        AlertsManager.initialize(applicationContext)

        // ✅ Initialize ThemeManager with context
        ThemeManager.initialize(applicationContext)

        // Initialize UserPreferencesManager
        val preferencesManager = UserPreferencesManager(this)

        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()

            // ✅ Load vehicles on app start
            LaunchedEffect(Unit) {
                scope.launch {
                    android.util.Log.d("MainActivity", "Loading user vehicles...")
                    VehicleManager.loadUserVehicles().onSuccess { vehicles ->
                        android.util.Log.d("MainActivity", "Loaded ${vehicles.size} vehicles")
                    }.onFailure { error ->
                        android.util.Log.e("MainActivity", "Failed to load vehicles: ${error.message}")
                    }
                }
            }

            Nfc_parkingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Check login/permissions state to determine start screen
                    val hasCompletedPermissions = preferencesManager.hasCompletedPermissions()
                    val firebaseUser = FirebaseAuth.getInstance().currentUser

                    val startDestination = when {
                        // ✅ Already logged in — skip everything, go straight to home
                        firebaseUser != null -> NavRoutes.HOME
                        // Permissions not done yet — show permission screen
                        !hasCompletedPermissions -> NavRoutes.PERMISSIONS
                        // Permissions done but not logged in — show login
                        else -> NavRoutes.AUTH
                    }

                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}