package com.example.nfc_parking.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class BookingStatus {
    ACTIVE, COMPLETED, CANCELLED
}

data class BookingHistoryItem(
    val booking: Booking,
    val status: BookingStatus
)

object BookingHistory {
    private val _history = mutableStateListOf<BookingHistoryItem>()
    val history: List<BookingHistoryItem> get() = _history.toList()

    private var preferences: SharedPreferences? = null
    private val gson = Gson()

    /**
     * Initialize with context - MUST BE CALLED FROM MainActivity
     */
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("booking_history", Context.MODE_PRIVATE)
        loadFromPreferences()
    }

    /**
     * Load saved bookings from SharedPreferences
     */
    private fun loadFromPreferences() {
        val json = preferences?.getString("history", null) ?: return
        try {
            val type = object : TypeToken<List<BookingHistoryItem>>() {}.type
            val savedHistory: List<BookingHistoryItem> = gson.fromJson(json, type)
            _history.clear()
            _history.addAll(savedHistory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save to SharedPreferences
     */
    private fun saveToPreferences() {
        val json = gson.toJson(_history.toList())
        preferences?.edit()?.putString("history", json)?.apply()
    }

    /**
     * Add a new booking
     */
    fun addBooking(booking: Booking) {
        val historyItem = BookingHistoryItem(
            booking = booking,
            status = BookingStatus.ACTIVE
        )
        _history.add(0, historyItem) // Add to top of list
        saveToPreferences()

        // ✅ TRIGGER BOOKING CONFIRMATION ALERT
        AlertManager.showBookingConfirmation(booking)
    }

    /**
     * Cancel a booking by ID
     */
    fun cancelBooking(bookingId: String) {
        val index = _history.indexOfFirst {
            it.booking.bookingId == bookingId && it.status == BookingStatus.ACTIVE
        }

        if (index != -1) {
            val bookingItem = _history[index]

            // ✅ TRIGGER REFUND ALERT BEFORE CANCELLING
            AlertManager.showCancellationRefund(bookingItem.booking)

            // Update status to cancelled
            _history[index] = bookingItem.copy(status = BookingStatus.CANCELLED)
            saveToPreferences()
        }
    }

    /**
     * Mark a booking as completed
     */
    fun completeBooking(bookingId: String) {
        val index = _history.indexOfFirst {
            it.booking.bookingId == bookingId && it.status == BookingStatus.ACTIVE
        }

        if (index != -1) {
            val bookingItem = _history[index]
            _history[index] = bookingItem.copy(status = BookingStatus.COMPLETED)
            saveToPreferences()
        }
    }

    /**
     * Get active bookings
     */
    fun getActiveBookings(): List<BookingHistoryItem> {
        return _history.filter { it.status == BookingStatus.ACTIVE }
    }

    /**
     * Get past bookings (completed or cancelled)
     */
    fun getPastBookings(): List<BookingHistoryItem> {
        return _history.filter {
            it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED
        }
    }

    /**
     * Clear all history
     */
    fun clearHistory() {
        _history.clear()
        saveToPreferences()
    }
}