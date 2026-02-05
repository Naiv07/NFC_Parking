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
                // ✅ Completely recreate the screen each time
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
                        coroutineScope.launch {
                            val result = BookingManager.confirmBooking(booking.bookingId)

                            if (result.isSuccess) {
                                BookingHistory.addBooking(booking)
                                currentStep = PaymentStep.CONFIRMATION
                            } else {
                                android.util.Log.e("PaymentFlow", "Failed to confirm booking: ${result.exceptionOrNull()?.message}")
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
                    onBack = onBackToMain
                )
            }
        }
    }
}