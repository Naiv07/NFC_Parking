package com.example.nfc_parking.data

data class Booking(
    // Firestore IDs
    val bookingId: String = "",
    val spaceId: String = "",
    val userId: String = "",
    val userName: String = "",

    // Location info
    val locationId: String = "",
    val locationName: String = "",
    val spaceLabel: String = "",
    val section: String = "",
    val floor: Int = 0,

    // Pricing & timing
    val pricePerHour: Double = 4.12,
    val totalHours: Int = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val totalPrice: Double = 0.0,

    // Status
    val status: String = "active", // pending, active, completed, cancelled
    val timestamp: Long = System.currentTimeMillis()
)