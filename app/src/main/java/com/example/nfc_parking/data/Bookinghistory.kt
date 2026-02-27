package com.example.nfc_parking.data

import androidx.compose.runtime.mutableStateListOf

/**
 * Booking status enum
 */
enum class BookingStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

/**
 * Booking history item
 */
data class BookingHistoryItem(
    val booking: Booking,
    val status: BookingStatus,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Singleton to manage booking history
 */
object BookingHistory {
    private val _history = mutableStateListOf<BookingHistoryItem>()
    val history: List<BookingHistoryItem> get() = _history.toList()

    /**
     * Add a booking to history
     */
    fun addBooking(booking: Booking, status: BookingStatus) {
        // Check if booking already exists
        val existingIndex = _history.indexOfFirst { it.booking.bookingId == booking.bookingId }

        if (existingIndex != -1) {
            // Update existing booking
            _history[existingIndex] = BookingHistoryItem(booking, status)
        } else {
            // Add new booking
            _history.add(BookingHistoryItem(booking, status))
        }
    }

    /**
     * Get all bookings with a specific status
     */
    fun getBookingsByStatus(status: BookingStatus): List<BookingHistoryItem> {
        return _history.filter { it.status == status }
    }

    /**
     * Get a specific booking by ID
     */
    fun getBookingById(bookingId: String): BookingHistoryItem? {
        return _history.find { it.booking.bookingId == bookingId }
    }

    /**
     * Update booking status
     */
    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        val index = _history.indexOfFirst { it.booking.bookingId == bookingId }
        if (index != -1) {
            val item = _history[index]
            _history[index] = item.copy(status = newStatus)
        }
    }

    /**
     * Remove a booking from history
     */
    fun removeBooking(bookingId: String) {
        _history.removeAll { it.booking.bookingId == bookingId }
    }

    /**
     * Clear all history
     */
    fun clearHistory() {
        _history.clear()
    }

    /**
     * Get active bookings count
     */
    fun getActiveBookingsCount(): Int {
        return _history.count { it.status == BookingStatus.ACTIVE }
    }

    /**
     * Get completed bookings count
     */
    fun getCompletedBookingsCount(): Int {
        return _history.count { it.status == BookingStatus.COMPLETED }
    }

    /**
     * Get cancelled bookings count
     */
    fun getCancelledBookingsCount(): Int {
        return _history.count { it.status == BookingStatus.CANCELLED }
    }
}