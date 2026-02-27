package com.example.nfc_parking.ui.home

import androidx.compose.ui.graphics.Color

data class ParkingLocation(
    val id: String,
    val name: String,
    val address: String,
    var distance: String = "",      // Keep var for distance updates
    var duration: String = "",       // Keep var for duration updates
    val pricePerHour: String,
    var availableSpots: Int,         // ✅ Keep var so it can be updated
    val totalSpots: Int = 100,       // ✅ ADD THIS - total capacity
    val accentColor: Color,
    val latitude: Double,
    val longitude: Double
) {
    // Helper to create a copy with updated spots
    fun withUpdatedSpots(newAvailableSpots: Int): ParkingLocation {
        return copy(availableSpots = newAvailableSpots)
    }
}