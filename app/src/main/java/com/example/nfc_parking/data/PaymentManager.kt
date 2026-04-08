package com.example.nfc_parking.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Keep it as UpiId instead of SavedUpiId
data class UpiId(
    val id: String,
    val upiId: String,
    val isDefault: Boolean = false
)

object PaymentManager {
    val savedCards = mutableStateListOf<SavedCard>()
    val savedUpiIds = mutableStateListOf<SavedUpiId>()

    var selectedCardId by mutableStateOf<String?>(null)
    var selectedPaymentType by mutableStateOf<PaymentType?>(null)
}