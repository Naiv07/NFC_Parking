package com.example.nfc_parking.data

import androidx.compose.ui.graphics.Color

enum class PaymentType {
    GPAY,
    APPLE_PAY,
    DEBIT_CARD,
    CREDIT_CARD,
    SLICE,
    SIMPL,
    AMAZON_PAY,
    UPI,
    CASH
}

data class SavedCard(
    val id: String,
    val cardholderName: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String = "",
    val cardType: PaymentType,
    val bankName: String = "",
    val isDefault: Boolean = false,
    val gradientColors: List<Color> = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
)

// ✅ Renamed to match usage in PaymentMethodScreen
data class SavedUpiId(
    val id: String,
    val upiId: String,
    val isDefault: Boolean = false
)