package com.example.nfc_parking.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nfc_parking.data.Booking

class BookingViewModel : ViewModel() {

    // Single source of truth for current booking
    val currentBooking = mutableStateOf<Booking?>(null)

    fun setBooking(booking: Booking) {
        currentBooking.value = booking
    }

    fun clearBooking() {
        currentBooking.value = null
    }
}
