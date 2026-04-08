package com.example.nfc_parking.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingConfirmationScreen(
    booking: Booking,
    onViewTicket: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentGreen = Color(0xFF39FF14)

    // ✅ CONFIRM BOOKING AND CREATE ALERT ON SCREEN LOAD
    LaunchedEffect(booking.bookingId) {
        android.util.Log.d("🔥 BookingConfirm", "==========================================")
        android.util.Log.d("🔥 BookingConfirm", "LaunchedEffect STARTED for: ${booking.bookingId}")
        android.util.Log.d("🔥 BookingConfirm", "Booking status: ${booking.status}")

        try {
            // 1. Confirm booking in Firebase (status = "active")
            android.util.Log.d("🔥 BookingConfirm", "Calling confirmBooking...")
            val result = com.example.nfc_parking.data.BookingManager.confirmBooking(booking.bookingId)

            result.onSuccess {
                android.util.Log.d("🔥 BookingConfirm", "✅✅✅ Booking confirmed successfully!")

                // 2. Add to local history
                android.util.Log.d("🔥 BookingConfirm", "Adding to local history...")
                com.example.nfc_parking.data.BookingHistory.addBooking(
                    booking,
                    com.example.nfc_parking.data.BookingStatus.ACTIVE
                )
                android.util.Log.d("🔥 BookingConfirm", "✅✅✅ Added to local history!")

                // 3. Create alert/notification
                android.util.Log.d("🔥 BookingConfirm", "Creating alert...")
                com.example.nfc_parking.data.AlertsManager.showBookingConfirmation(booking)
                android.util.Log.d("🔥 BookingConfirm", "✅✅✅ Alert created!")
                android.util.Log.d("🔥 BookingConfirm", "==========================================")

            }.onFailure { error ->
                android.util.Log.e("🔥 BookingConfirm", "❌❌❌ FAILED to confirm!")
                android.util.Log.e("🔥 BookingConfirm", "Error: ${error.message}")
                android.util.Log.e("🔥 BookingConfirm", "Stack trace:", error)
                android.util.Log.d("🔥 BookingConfirm", "==========================================")
            }
        } catch (e: Exception) {
            android.util.Log.e("🔥 BookingConfirm", "❌❌❌ EXCEPTION!")
            android.util.Log.e("🔥 BookingConfirm", "Error: ${e.message}", e)
            android.util.Log.d("🔥 BookingConfirm", "==========================================")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())  // ✅ ADD SCROLL
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Booking Confirmation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(accentGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(accentGreen.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = accentGreen,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Booking Confirmed!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your parking spot is secured.",
            fontSize = 14.sp,
            color = subtextColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Booking ID Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Booking ID",
                    fontSize = 12.sp,
                    color = subtextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = booking.bookingId,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Booking Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Booking summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location
                SummaryRow(
                    icon = Icons.Default.Garage,
                    text = booking.locationName,
                    textColor = textColor,
                    subtextColor = subtextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Space
                SummaryRow(
                    icon = Icons.Default.LocationOn,
                    text = booking.spaceLabel,
                    textColor = textColor,
                    subtextColor = subtextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time
                SummaryRow(
                    icon = Icons.Default.Schedule,
                    text = "${formatDateTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                    textColor = textColor,
                    subtextColor = subtextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                SummaryRow(
                    icon = Icons.Default.CreditCard,
                    text = "Amount Paid: Rs.${String.format("%.2f", booking.totalPrice)}",
                    textColor = textColor,
                    subtextColor = subtextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // QR Code Placeholder
        Card(
            modifier = Modifier.size(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QR",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "CODE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Scan at parking entry/exit",
            fontSize = 12.sp,
            color = subtextColor
        )

        Spacer(modifier = Modifier.weight(1f))

        // View Ticket Button
        Button(
            onClick = onViewTicket,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentGreen
            )
        ) {
            Text(
                text = "View Ticket",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = subtextColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

private fun formatDateTime(timeMillis: Long): String {
    val format = SimpleDateFormat("EEE, MMM dd, yyyy | h:mm a", Locale.getDefault())
    return format.format(Date(timeMillis))
}

private fun formatTime(timeMillis: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timeMillis))
}