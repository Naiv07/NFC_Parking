package com.example.nfc_parking.data

/**
 * Booking data class representing a parking space reservation
 *
 * Status values:
 * - "pending": Space reserved but payment not confirmed (doesn't show as booked)
 * - "active": Payment confirmed, space is booked (shows as black/occupied)
 * - "cancelled": User cancelled the booking
 * - "completed": Booking time has ended normally
 * - "expired": Booking expired before confirmation
 */
data class Booking(
    val bookingId: String = "",
    val spaceId: String = "",
    val userId: String = "",
    val userName: String = "",
    val locationId: String = "",
    val locationName: String = "",
    val spaceLabel: String = "",

    // Time fields
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val cancelledAt: Long? = null,
    val completedAt: Long? = null,
    val expiredAt: Long? = null,

    // Pricing
    val pricePerHour: Double = 4.12,
    val totalHours: Int = 0,
    val totalPrice: Double = 0.0,

    // Status: pending, active, cancelled, completed, expired
    val status: String = "pending",

    // Optional: Payment information
    val paymentId: String? = null,
    val paymentMethod: String? = null,

    // Optional: Vehicle information
    val vehiclePlate: String? = null,
    val vehicleModel: String? = null
) {
    /**
     * Check if the booking is currently active (within time range and active status)
     */
    fun isActive(): Boolean {
        val now = System.currentTimeMillis()
        return status == "active" && now >= startTime && now < endTime
    }

    /**
     * Check if the booking has expired (past end time)
     */
    fun hasExpired(): Boolean {
        return System.currentTimeMillis() >= endTime
    }

    /**
     * Check if the booking can be cancelled
     */
    fun canBeCancelled(): Boolean {
        return (status == "pending" || status == "active") && !hasExpired()
    }

    /**
     * Get remaining time in milliseconds
     */
    fun getRemainingTime(): Long {
        return maxOf(0L, endTime - System.currentTimeMillis())
    }

    /**
     * Get remaining time in minutes
     */
    fun getRemainingMinutes(): Int {
        return (getRemainingTime() / (60 * 1000)).toInt()
    }

    /**
     * Get duration in hours
     */
    fun getDurationHours(): Int {
        return ((endTime - startTime) / (60 * 60 * 1000)).toInt()
    }

    /**
     * Get human-readable status
     */
    fun getStatusDisplay(): String {
        return when (status) {
            "pending" -> "Pending Payment"
            "active" -> "Active"
            "cancelled" -> "Cancelled"
            "completed" -> "Completed"
            "expired" -> "Expired"
            else -> status.capitalize()
        }
    }

    /**
     * Convert to map for Firestore updates
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "bookingId" to bookingId,
            "spaceId" to spaceId,
            "userId" to userId,
            "userName" to userName,
            "locationId" to locationId,
            "locationName" to locationName,
            "spaceLabel" to spaceLabel,
            "startTime" to startTime,
            "endTime" to endTime,
            "createdAt" to createdAt,
            "cancelledAt" to cancelledAt,
            "completedAt" to completedAt,
            "expiredAt" to expiredAt,
            "pricePerHour" to pricePerHour,
            "totalHours" to totalHours,
            "totalPrice" to totalPrice,
            "status" to status,
            "paymentId" to paymentId,
            "paymentMethod" to paymentMethod,
            "vehiclePlate" to vehiclePlate,
            "vehicleModel" to vehicleModel
        )
    }
}