package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.Booking
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingConfirmationScreen(
    booking: Booking,
    onViewTicket: () -> Unit,
    onBackToHome: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    // Format time helper
    fun formatTime(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    fun formatDate(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Booking Confirmation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f))
                            .border(4.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Booking Confirmed!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your parking spot is secured.",
                        fontSize = 14.sp,
                        color = subtextColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Booking ID", fontSize = 12.sp, color = subtextColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = booking.bookingId,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Booking summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        ConfirmationSummaryItem(
                            icon = Icons.Default.Garage,
                            text = booking.locationName,
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        ConfirmationSummaryItem(
                            icon = Icons.Default.LocationOn,
                            text = booking.spaceLabel,
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        ConfirmationSummaryItem(
                            icon = Icons.Default.AccessTime,
                            text = "${formatDate(booking.startTime)} | ${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        ConfirmationSummaryItem(
                            icon = Icons.Default.Payment,
                            text = "Amount Paid: ₹${String.format("%.2f", booking.totalPrice)}",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR\nCODE",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Scan at parking entry/exit",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onViewTicket,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("View Ticket", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToHome) {
                Text("Back to Home", color = subtextColor)
            }
        }
    }
}

@Composable
private fun ConfirmationSummaryItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = subtextColor,
            modifier = Modifier.size(20.dp)
        )
        Text(text = text, fontSize = 14.sp, color = textColor)
    }
}