package com.example.nfc_parking.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.data.BookingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing parking bookings with proper lifecycle handling
 *
 * This ViewModel demonstrates best practices for using BookingManager:
 * - Automatic listener cleanup via lifecycle
 * - Error handling and user feedback
 * - Loading states
 * - Reactive UI updates
 */
class BookingViewModel : ViewModel() {

    // Current booking (for payment flow)
    private val _currentBooking = mutableStateOf<Booking?>(null)
    val currentBooking: State<Booking?> = _currentBooking

    // UI State
    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Initial)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // User's bookings
    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    // Current location being observed
    private var currentLocationId: String? = null

    /**
     * Set current booking (for payment flow)
     */
    fun setBooking(booking: Booking) {
        _currentBooking.value = booking
    }

    /**
     * Clear current booking
     */
    fun clearBooking() {
        _currentBooking.value = null
    }

    /**
     * Start observing a parking location
     * Call this when user enters a parking location screen
     */
    fun observeLocation(locationId: String) {
        if (currentLocationId != locationId) {
            currentLocationId = locationId
            BookingManager.startListening(locationId)

            // Also trigger a cleanup of expired bookings for this location
            viewModelScope.launch {
                BookingManager.cleanupExpiredBookings(locationId)
            }
        }
    }

    /**
     * Stop observing the current location
     * Call this when user leaves the parking location screen
     */
    fun stopObserving() {
        BookingManager.stopListening()
        currentLocationId = null
    }

    /**
     * Reserve a parking space (Step 1 of booking process)
     */
    fun reserveSpace(
        spaceId: String,
        userId: String,
        userName: String,
        locationId: String,
        locationName: String,
        spaceLabel: String,
        durationHours: Int,
        pricePerHour: Double = 4.12
    ) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading("Reserving space...")

            when (val result = BookingManager.reserveSpace(
                spaceId, userId, userName, locationId,
                locationName, spaceLabel, durationHours, pricePerHour
            )) {
                is BookingResult.Success -> {
                    _uiState.value = BookingUiState.ReservationSuccess(result.booking)
                }
                is BookingResult.Error -> {
                    _uiState.value = BookingUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Confirm a booking after payment (Step 2 of booking process)
     */
    fun confirmBooking(bookingId: String, paymentId: String? = null) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading("Confirming booking...")

            val result = BookingManager.confirmBooking(bookingId)
            if (result.isSuccess) {
                _uiState.value = BookingUiState.BookingConfirmed(bookingId)
                loadUserBookings(getCurrentUserId()) // Refresh user bookings
            } else {
                _uiState.value = BookingUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to confirm booking"
                )
            }
        }
    }

    /**
     * Cancel a booking
     */
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading("Cancelling booking...")

            val result = BookingManager.cancelBooking(bookingId)
            if (result.isSuccess) {
                _uiState.value = BookingUiState.BookingCancelled(bookingId)
                loadUserBookings(getCurrentUserId()) // Refresh user bookings
            } else {
                _uiState.value = BookingUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to cancel booking"
                )
            }
        }
    }

    /**
     * Load user's active bookings
     */
    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            val bookings = BookingManager.getUserBookings(userId)
            _userBookings.value = bookings
        }
    }

    /**
     * Load user's booking history
     */
    fun loadUserBookingHistory(userId: String, limit: Int = 50) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading("Loading history...")

            val history = BookingManager.getUserBookingHistory(userId, limit)
            _uiState.value = BookingUiState.HistoryLoaded(history)
        }
    }

    /**
     * Check if a space is currently booked
     */
    fun isSpaceBooked(spaceId: String): Boolean {
        return BookingManager.isSpaceBooked(spaceId)
    }

    /**
     * Get all currently booked spaces
     */
    fun getBookedSpaces(): Set<String> {
        return BookingManager.bookedSpaces.value
    }

    /**
     * Reset UI state
     */
    fun resetUiState() {
        _uiState.value = BookingUiState.Initial
    }

    /**
     * Helper to get current user ID (implement based on your auth system)
     */
    private fun getCurrentUserId(): String {
        // TODO: Implement with your authentication system
        // Example: return FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return ""
    }

    /**
     * Clean up when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        stopObserving()
    }
}

/**
 * Sealed class representing different UI states
 */
sealed class BookingUiState {
    object Initial : BookingUiState()
    data class Loading(val message: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
    data class ReservationSuccess(val booking: Booking) : BookingUiState()
    data class BookingConfirmed(val bookingId: String) : BookingUiState()
    data class BookingCancelled(val bookingId: String) : BookingUiState()
    data class HistoryLoaded(val bookings: List<Booking>) : BookingUiState()
}