package com.example.nfc_parking.ui.vehicles

data class Vehicle(
    val id: String,
    val name: String,
    val model: String,
    val type: VehicleType,
    val imageUrl: String? = null
)

enum class VehicleType {
    CAR, BIKE, EV, TRUCK
}