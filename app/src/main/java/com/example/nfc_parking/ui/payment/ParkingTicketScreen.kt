package com.example.nfc_parking.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.nfc_parking.ui.components.QRCodeImage  // ✅ Import QR component
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ParkingTicketScreen(
    booking: Booking,
    onBack: () -> Unit = {},
    onGetDirections: () -> Unit = {},
    onShareReceipt: () -> Unit = {},
    onSupport: () -> Unit = {}
) {
    var currentBooking by remember { mutableStateOf(booking) }
    LaunchedEffect(booking) {
        currentBooking = booking
    }
    Text(text = "Duration: ${currentBooking.totalHours}h")  // ✅ Will update
    Text(text = "Total: Rs.${currentBooking.totalPrice}")    // ✅ Will update
    Text(text = formatTime(currentBooking.endTime))          // ✅ Will update
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentGreen = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF10B981)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(cardColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            Text(
                text = "Parking Ticket",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentGreen
            ) {
                Text(
                    text = "Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Parking Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = accentGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Location Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = booking.locationName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = booking.spaceLabel,
                            fontSize = 14.sp,
                            color = subtextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Booking Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    TicketDetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = formatDate(booking.startTime),
                        textColor = textColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TicketDetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Time",
                        value = "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                        textColor = textColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TicketDetailRow(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = "${booking.totalHours} ${if (booking.totalHours == 1) "hour" else "hours"}",
                        textColor = textColor,
                        subtextColor = subtextColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(color = subtextColor.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Amount Paid",
                                fontSize = 14.sp,
                                color = subtextColor
                            )
                        }
                        Text(
                            text = "₹${String.format("%.2f", booking.totalPrice)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Booking ID
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BOOKING ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtextColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = booking.bookingId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen,
                    textAlign = TextAlign.Center,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ REAL QR CODE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Generate QR code with booking ID
                    QRCodeImage(
                        content = booking.bookingId,
                        size = 220.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan for Entry/Exit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Get Directions
            ActionButton(
                icon = Icons.Default.Navigation,
                label = "Directions",
                onClick = onGetDirections,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                accentColor = accentGreen
            )

            // Share Receipt
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShareReceipt,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                accentColor = accentGreen
            )

            // Support
            ActionButton(
                icon = Icons.Default.HeadsetMic,
                label = "Support",
                onClick = onSupport,
                modifier = Modifier.weight(1f),
                cardColor = cardColor,
                textColor = textColor,
                accentColor = accentGreen
            )
        }
    }
}

@Composable
private fun TicketDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = subtextColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = subtextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(timeMillis: Long): String {
    val format = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    return format.format(Date(timeMillis))
}

private fun formatTime(timeMillis: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timeMillis))
}