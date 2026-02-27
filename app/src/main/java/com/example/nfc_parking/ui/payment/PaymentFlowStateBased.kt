package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.BookingHistory
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.data.BookingStatus
import com.example.nfc_parking.data.AlertsManager
import com.example.nfc_parking.ui.booking.BookingConfirmationScreen
import com.example.nfc_parking.ui.ticket.ParkingTicketScreen
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
    val coroutineScope = rememberCoroutineScope()

    // ✅ Debug logging
    LaunchedEffect(currentStep) {
        android.util.Log.d("PaymentFlow", "Current step: $currentStep")
    }

    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        when (currentStep) {
            PaymentStep.SELECT_PAYMENT -> {
                DisposableEffect(Unit) {
                    android.util.Log.d("PaymentFlow", "PaymentMethodScreen mounted")
                    onDispose {
                        android.util.Log.d("PaymentFlow", "PaymentMethodScreen disposed")
                    }
                }

                PaymentMethodScreen(
                    onBack = onBackToMain,
                    onPaymentSelected = {
                        android.util.Log.d("PaymentFlow", "Moving to PIN_ENTRY")
                        currentStep = PaymentStep.PIN_ENTRY
                    }
                )
            }

            PaymentStep.PIN_ENTRY -> {
                DisposableEffect(Unit) {
                    android.util.Log.d("PaymentFlow", "PinEntryScreen mounted")
                    onDispose {
                        android.util.Log.d("PaymentFlow", "PinEntryScreen disposed")
                    }
                }

                PinEntryScreen(
                    onBack = {
                        android.util.Log.d("PaymentFlow", "Back pressed from PIN, going to SELECT_PAYMENT")
                        currentStep = PaymentStep.SELECT_PAYMENT
                    },
                    onPinConfirmed = {
                        android.util.Log.d("PaymentFlow", "💳 PIN confirmed! Starting booking confirmation...")
                        coroutineScope.launch {
                            try {
                                // ✅ FIX #1: Confirm booking (status = "active")
                                android.util.Log.d("PaymentFlow", "Calling confirmBooking for: ${booking.bookingId}")
                                val result = BookingManager.confirmBooking(booking.bookingId)

                                result.onSuccess {
                                    android.util.Log.d("PaymentFlow", "✅ Booking confirmed successfully!")

                                    // ✅ FIX #2: Add to history as ACTIVE (not CANCELLED!)
                                    BookingHistory.addBooking(booking, BookingStatus.ACTIVE)
                                    android.util.Log.d("PaymentFlow", "✅ Added to history as ACTIVE")

                                    // ✅ FIX #3: Create alert
                                    AlertsManager.showBookingConfirmation(booking)
                                    android.util.Log.d("PaymentFlow", "✅ Alert created!")

                                    // Move to confirmation screen
                                    currentStep = PaymentStep.CONFIRMATION

                                }.onFailure { error ->
                                    android.util.Log.e("PaymentFlow", "❌ Failed to confirm booking: ${error.message}")
                                    // Still show confirmation screen even if Firebase fails
                                    // (booking was already created as pending)
                                    currentStep = PaymentStep.CONFIRMATION
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PaymentFlow", "❌ Exception: ${e.message}", e)
                                // Still show confirmation screen
                                currentStep = PaymentStep.CONFIRMATION
                            }
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
                        // Go back to confirmation screen instead of home
                        currentStep = PaymentStep.CONFIRMATION
                    }
                )
            }
        }
    }
}