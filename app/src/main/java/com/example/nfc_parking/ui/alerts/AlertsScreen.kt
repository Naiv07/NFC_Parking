package com.example.nfc_parking.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.*
import com.example.nfc_parking.ui.components.ExtendTimeDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(
    onBack: () -> Unit = {},
    onNavigateToTicket: (String) -> Unit = {}  // ✅ Navigate to ticket with booking ID
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    // State for extend time dialog
    var showExtendDialog by remember { mutableStateOf(false) }
    var selectedBookingForExtend by remember { mutableStateOf<Booking?>(null) }

    // Color scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentGreen = Color(0xFF39FF14)

    val alerts = AlertsManager.alerts

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alerts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            if (alerts.isNotEmpty()) {
                TextButton(onClick = { AlertsManager.clearAll() }) {
                    Text(
                        text = "Clear All",
                        color = accentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Alerts List
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Alerts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You'll see notifications here",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts.reversed()) { alert ->
                    AlertCard(
                        alert = alert,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        cardColor = cardColor,
                        accentGreen = accentGreen,
                        onDismiss = {
                            AlertsManager.removeAlert(alert.id)
                        },
                        onExtendTime = {
                            // Load booking and show extend dialog
                            alert.bookingId?.let { bookingId ->
                                scope.launch {
                                    val booking = BookingManager.getBooking(bookingId)
                                    if (booking != null) {
                                        selectedBookingForExtend = booking
                                        showExtendDialog = true
                                    }
                                }
                            }
                        },
                        onViewBooking = {
                            // Navigate to ticket screen
                            alert.bookingId?.let { bookingId ->
                                onNavigateToTicket(bookingId)
                            }
                        },
                        onSupport = {
                            // TODO: Implement support
                            android.util.Log.d("Alerts", "Support clicked for alert: ${alert.id}")
                        }
                    )
                }
            }
        }
    }

    // Extend Time Dialog
    if (showExtendDialog && selectedBookingForExtend != null) {
        ExtendTimeDialog(
            booking = selectedBookingForExtend!!,
            onDismiss = {
                showExtendDialog = false
                selectedBookingForExtend = null
            },
            onConfirm = { additionalHours ->
                scope.launch {
                    android.util.Log.d("🕐 ExtendTime", "Extending booking by $additionalHours hours")

                    val booking = selectedBookingForExtend!!

                    // ✅ Extend booking in Firebase
                    val result = BookingManager.extendBooking(booking.bookingId, additionalHours)

                    result.onSuccess { updatedBooking ->
                        android.util.Log.d("🕐 ExtendTime", "✅ Booking extended successfully!")

                        // ✅ Update local history with new booking data
                        BookingHistory.addBooking(updatedBooking, BookingStatus.ACTIVE)
                        android.util.Log.d("🕐 ExtendTime", "✅ Local history updated")

                        // ✅ Show success alert
                        AlertsManager.addAlert(
                            Alert(
                                id = "extend_${booking.bookingId}_${System.currentTimeMillis()}",
                                title = "Time Extended Successfully",
                                message = "Your parking time has been extended by $additionalHours ${if (additionalHours == 1) "hour" else "hours"}",
                                location = booking.locationName,
                                spotNumber = booking.spaceLabel,
                                timeRemaining = "${updatedBooking.totalHours}h",
                                endTime = updatedBooking.endTime,
                                urgency = AlertUrgency.LOW,
                                bookingId = booking.bookingId,
                                amount = updatedBooking.totalPrice
                            )
                        )
                        android.util.Log.d("🕐 ExtendTime", "✅ Success alert created")

                    }.onFailure { error ->
                        android.util.Log.e("🕐 ExtendTime", "❌ Failed to extend: ${error.message}")

                        // Show error alert
                        AlertsManager.addAlert(
                            Alert(
                                id = "extend_error_${System.currentTimeMillis()}",
                                title = "Extension Failed",
                                message = "Failed to extend parking time. Please try again.",
                                location = booking.locationName,
                                spotNumber = booking.spaceLabel,
                                timeRemaining = "Error",
                                endTime = booking.endTime,
                                urgency = AlertUrgency.HIGH,
                                bookingId = booking.bookingId
                            )
                        )
                    }

                    showExtendDialog = false
                    selectedBookingForExtend = null
                }
            }
        )
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    accentGreen: Color,
    onDismiss: () -> Unit,
    onExtendTime: () -> Unit,
    onViewBooking: () -> Unit,
    onSupport: () -> Unit
) {
    // Determine alert type from title
    val isCancelled = alert.title.contains("Cancelled", ignoreCase = true)
    val isExpiring = alert.title.contains("Expiring", ignoreCase = true)
    val isConfirmed = alert.title.contains("Confirmed", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon and dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Alert icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (alert.urgency) {
                                    AlertUrgency.HIGH -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    AlertUrgency.MEDIUM -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    AlertUrgency.LOW -> accentGreen.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (alert.urgency) {
                                AlertUrgency.HIGH -> Icons.Default.Warning
                                AlertUrgency.MEDIUM -> Icons.Default.Schedule
                                AlertUrgency.LOW -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (alert.urgency) {
                                AlertUrgency.HIGH -> Color(0xFFEF4444)
                                AlertUrgency.MEDIUM -> Color(0xFFF59E0B)
                                AlertUrgency.LOW -> accentGreen
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = subtextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = subtextColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = alert.location,
                    fontSize = 13.sp,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spot",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = alert.spotNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time Left",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = alert.timeRemaining,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isCancelled -> Color(0xFFEF4444)
                            isExpiring -> Color(0xFFF59E0B)
                            else -> accentGreen
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ends At",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = formatTime(alert.endTime),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons - Different for each alert type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isCancelled -> {
                        // Cancelled: Support + View Booking
                        OutlinedButton(
                            onClick = onSupport,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, subtextColor)
                        ) {
                            Text(
                                text = "Support",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onViewBooking,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentGreen
                            )
                        ) {
                            Text(
                                text = "View Booking",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    isExpiring || isConfirmed -> {
                        // Expiring/Confirmed: Extend Time + View Booking
                        OutlinedButton(
                            onClick = onExtendTime,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, subtextColor)
                        ) {
                            Text(
                                text = "Extend Time",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onViewBooking,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentGreen
                            )
                        ) {
                            Text(
                                text = "View Booking",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timeMillis))
}