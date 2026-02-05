package com.example.nfc_parking.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.BookingHistory
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.data.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.nfc_parking.data.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsHistoryScreen(
    onBack: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    var userBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    // Theme colors
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    // Load bookings on start and refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
                val firebaseBookings = BookingManager.getUserBookings(userId)
                val localHistory = BookingHistory.history

                // Combine Firebase bookings with local history
                val combinedMap = mutableMapOf<String, Booking>()

                // Add Firebase bookings
                firebaseBookings.forEach { booking ->
                    combinedMap[booking.bookingId] = booking
                }

                // Add local history bookings (may override Firebase with cancelled status)
                localHistory.forEach { historyItem ->
                    val booking = historyItem.booking
                    // Update status based on local history
                    val updatedBooking = booking.copy(
                        status = when (historyItem.status) {
                            BookingStatus.ACTIVE -> "active"
                            BookingStatus.CANCELLED -> "cancelled"
                            BookingStatus.COMPLETED -> "completed"
                        }
                    )
                    combinedMap[booking.bookingId] = updatedBooking
                }

                userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
            }
            isLoading = false
            kotlinx.coroutines.delay(5000)
        }
    }

    // Separate active and past bookings
    val currentTime = System.currentTimeMillis()
    val activeBookings = userBookings.filter {
        it.endTime > currentTime && it.status == "active"
    }
    val pastBookings = userBookings.filter {
        it.endTime <= currentTime || it.status == "cancelled" || it.status == "expired" || it.status == "completed"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopBar(
                onBack = onBack,
                onRefresh = {
                    scope.launch {
                        isLoading = true
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        if (userId.isNotEmpty()) {
                            val firebaseBookings = BookingManager.getUserBookings(userId)
                            val localHistory = BookingHistory.history

                            val combinedMap = mutableMapOf<String, Booking>()
                            firebaseBookings.forEach { booking ->
                                combinedMap[booking.bookingId] = booking
                            }
                            localHistory.forEach { historyItem ->
                                val booking = historyItem.booking
                                val updatedBooking = booking.copy(
                                    status = when (historyItem.status) {
                                        BookingStatus.ACTIVE -> "active"
                                        BookingStatus.CANCELLED -> "cancelled"
                                        BookingStatus.COMPLETED -> "completed"
                                    }
                                )
                                combinedMap[booking.bookingId] = updatedBooking
                            }
                            userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
                        }
                        isLoading = false
                    }
                },
                cardColor = cardColor,
                textColor = textColor
            )

            // Content
            when {
                isLoading -> LoadingContent(accentColor)
                userBookings.isEmpty() -> EmptyContent(textColor, subtextColor)
                else -> BookingsList(
                    activeBookings = activeBookings,
                    pastBookings = pastBookings,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    cardColor = cardColor,
                    accentColor = accentColor,
                    onCancelClick = { booking ->
                        bookingToCancel = booking
                        showCancelDialog = true
                    }
                )
            }
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelDialog && bookingToCancel != null) {
        CancelBookingDialog(
            booking = bookingToCancel!!,
            isCancelling = isCancelling,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                scope.launch {
                    isCancelling = true
                    val result = BookingManager.cancelBooking(bookingToCancel!!.bookingId)

                    if (result.isSuccess) {
                        // Update local history cache
                        BookingHistory.cancelBooking(bookingToCancel!!.bookingId)

                        // Refresh bookings list
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        if (userId.isNotEmpty()) {
                            val firebaseBookings = BookingManager.getUserBookings(userId)
                            val localHistory = BookingHistory.history

                            val combinedMap = mutableMapOf<String, Booking>()
                            firebaseBookings.forEach { booking ->
                                combinedMap[booking.bookingId] = booking
                            }
                            localHistory.forEach { historyItem ->
                                val booking = historyItem.booking
                                val updatedBooking = booking.copy(
                                    status = when (historyItem.status) {
                                        BookingStatus.ACTIVE -> "active"
                                        BookingStatus.CANCELLED -> "cancelled"
                                        BookingStatus.COMPLETED -> "completed"
                                    }
                                )
                                combinedMap[booking.bookingId] = updatedBooking
                            }
                            userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
                        }
                    }

                    isCancelling = false
                    showCancelDialog = false
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    cardColor: Color,
    textColor: Color
) {
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
                .background(cardColor, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textColor
            )
        }

        Text(
            text = "My Bookings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .background(cardColor, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = textColor
            )
        }
    }
}

@Composable
private fun LoadingContent(accentColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = accentColor)
    }
}

@Composable
private fun EmptyContent(textColor: Color, subtextColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = subtextColor,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Bookings Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your parking history will appear here",
                fontSize = 14.sp,
                color = subtextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BookingsList(
    activeBookings: List<Booking>,
    pastBookings: List<Booking>,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    accentColor: Color,
    onCancelClick: (Booking) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Bookings Section
        if (activeBookings.isNotEmpty()) {
            item {
                Text(
                    text = "Active (${activeBookings.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            items(activeBookings.size) { index ->
                BookingCard(
                    booking = activeBookings[index],
                    isActive = true,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    accentColor = accentColor,
                    onCancel = { onCancelClick(activeBookings[index]) }
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Past Bookings Section
        if (pastBookings.isNotEmpty()) {
            item {
                Text(
                    text = "Past (${pastBookings.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            items(pastBookings.size) { index ->
                BookingCard(
                    booking = pastBookings[index],
                    isActive = false,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    accentColor = subtextColor
                )
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isActive: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    accentColor: Color,
    onCancel: () -> Unit = {}
) {
    val statusColor = when (booking.status) {
        "active" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        "cancelled" -> Color(0xFFEF4444)
        "expired" -> Color(0xFF6B7280)
        "completed" -> Color(0xFF6B7280)
        else -> subtextColor
    }

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
            // Header: Location + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Garage,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = booking.locationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Booking Details
            BookingDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Space",
                value = booking.spaceLabel,
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingDetailRow(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = booking.startTime.formatDate(),
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingDetailRow(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = "${booking.startTime.formatTime()} - ${booking.endTime.formatTime()}",
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingDetailRow(
                icon = Icons.Default.Timer,
                label = "Duration",
                value = "${booking.totalHours} ${if (booking.totalHours == 1) "hour" else "hours"}",
                textColor = textColor,
                subtextColor = subtextColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = subtextColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Price + Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Rs.${String.format("%.2f", booking.totalPrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                if (isActive && booking.status == "active") {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Booking ID
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ID: ${booking.bookingId}",
                fontSize = 10.sp,
                color = subtextColor.copy(alpha = 0.6f),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun BookingDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = subtextColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = subtextColor,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun CancelBookingDialog(
    booking: Booking,
    isCancelling: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                text = "Cancel Booking?",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to cancel this booking?",
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Location: ${booking.locationName}",
                    fontSize = 14.sp,
                    color = subtextColor
                )
                Text(
                    text = "Space: ${booking.spaceLabel}",
                    fontSize = 14.sp,
                    color = subtextColor
                )
                Text(
                    text = "Amount: Rs.${String.format("%.2f", booking.totalPrice)}",
                    fontSize = 14.sp,
                    color = subtextColor
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Cancel Booking",
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCancelling
            ) {
                Text(
                    text = "Keep Booking",
                    color = textColor
                )
            }
        }
    )
}

// Extension functions for date/time formatting
private fun Long.formatDate(): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))
}

private fun Long.formatTime(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}