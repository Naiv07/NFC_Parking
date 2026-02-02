package com.example.nfc_parking.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SavedUpiId(
    val id: String,
    val upiId: String
)

object PaymentManager {
    val savedCards = mutableStateListOf<SavedCard>()
    val savedUpiIds = mutableStateListOf<SavedUpiId>()

    var selectedCardId by mutableStateOf<String?>(null)
    var selectedPaymentType by mutableStateOf<PaymentType?>(null)
}