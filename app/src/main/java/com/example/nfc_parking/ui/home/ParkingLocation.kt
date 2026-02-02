package com.example.nfc_parking.ui.home

import androidx.compose.ui.graphics.Color

data class ParkingLocation(
    val id: String,
    val name: String,
    val address: String,
    val distance: String,
    val duration: String,
    val pricePerHour: String,
    val availableSpots: Int,
    val accentColor: Color
)