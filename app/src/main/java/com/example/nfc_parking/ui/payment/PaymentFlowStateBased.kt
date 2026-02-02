package com.example.nfc_parking.ui.payment

import androidx.compose.runtime.*
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.BookingManager
import kotlinx.coroutines.launch

enum class PaymentStep {
    SELECT_PAYMENT,
    PIN_ENTRY,
    CONFIRMATION,
    TICKET
}

@Composable
fun PaymentFlowStateBased(
    booking: Booking,
    onBackToMain: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PaymentStep.SELECT_PAYMENT) }
    val scope = rememberCoroutineScope()

    // ✅ Track if booking was confirmed
    var isBookingConfirmed by remember { mutableStateOf(false) }

    when (currentStep) {
        PaymentStep.SELECT_PAYMENT -> {
            PaymentMethodScreen(
                onBack = {
                    // ✅ Cancel pending booking if user backs out
                    if (!isBookingConfirmed) {
                        scope.launch {
                            BookingManager.cancelPendingBooking(booking.bookingId)
                        }
                    }
                    onBackToMain()
                },
                onPaymentSelected = {
                    currentStep = PaymentStep.PIN_ENTRY
                }
            )
        }

        PaymentStep.PIN_ENTRY -> {
            PinEntryScreen(
                onBack = {
                    currentStep = PaymentStep.SELECT_PAYMENT
                },
                onPinConfirmed = {
                    // ✅ Confirm the booking when PIN is verified
                    scope.launch {
                        BookingManager.confirmBooking(booking.bookingId)
                        isBookingConfirmed = true
                        currentStep = PaymentStep.CONFIRMATION
                    }
                }
            )
        }

        PaymentStep.CONFIRMATION -> {
            BookingConfirmationScreen(
                booking = booking,
                onViewTicket = {
                    currentStep = PaymentStep.TICKET
                },
                onBackToHome = onBackToMain
            )
        }

        PaymentStep.TICKET -> {
            ParkingTicketScreen(
                booking = booking,
                onBack = {
                    currentStep = PaymentStep.CONFIRMATION
                }
            )
        }
    }
}