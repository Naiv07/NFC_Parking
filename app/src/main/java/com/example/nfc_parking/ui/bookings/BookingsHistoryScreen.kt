package com.example.nfc_parking.ui.bookings

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingsHistoryScreen(
    onBack: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    BackHandler { onBack() }

    var userBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentGreen = Color(0xFF39FF14)

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Refresh every 10 seconds

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
                isLoading = true

                val firebaseBookings = BookingManager.getUserBookings(userId)
                val localHistory = BookingHistory.history

                val combinedMap = mutableMapOf<String, Booking>()
                firebaseBookings.forEach { combinedMap[it.bookingId] = it }
                localHistory.forEach { historyItem ->
                    val updatedBooking = historyItem.booking.copy(
                        status = when (historyItem.status) {
                            BookingStatus.ACTIVE -> "active"
                            BookingStatus.CANCELLED -> "cancelled"
                            BookingStatus.COMPLETED -> "completed"
                        }
                    )
                    combinedMap[historyItem.booking.bookingId] = updatedBooking
                }

                userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
                isLoading = false
            }
        }
    }

    // Load bookings ONCE on start
    LaunchedEffect(Unit) {
        android.util.Log.d("📋 BookingsHistory", "=== STARTING TO LOAD BOOKINGS ===")
        isLoading = true
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        android.util.Log.d("📋 BookingsHistory", "User ID: $userId")

        if (userId.isNotEmpty()) {
            android.util.Log.d("📋 BookingsHistory", "Fetching from Firebase...")
            val firebaseBookings = BookingManager.getUserBookings(userId)
            android.util.Log.d("📋 BookingsHistory", "Firebase bookings: ${firebaseBookings.size}")
            firebaseBookings.forEach {
                android.util.Log.d("📋 BookingsHistory", "  - FB: ${it.bookingId} | ${it.locationName} | status: ${it.status}")
            }

            android.util.Log.d("📋 BookingsHistory", "Fetching from local history...")
            val localHistory = BookingHistory.history
            android.util.Log.d("📋 BookingsHistory", "Local history: ${localHistory.size}")
            localHistory.forEach {
                android.util.Log.d("📋 BookingsHistory", "  - Local: ${it.booking.bookingId} | ${it.booking.locationName} | status: ${it.status}")
            }

            val combinedMap = mutableMapOf<String, Booking>()
            firebaseBookings.forEach { combinedMap[it.bookingId] = it }
            localHistory.forEach { historyItem ->
                val updatedBooking = historyItem.booking.copy(
                    status = when (historyItem.status) {
                        BookingStatus.ACTIVE -> "active"
                        BookingStatus.CANCELLED -> "cancelled"
                        BookingStatus.COMPLETED -> "completed"
                    }
                )
                combinedMap[historyItem.booking.bookingId] = updatedBooking
            }

            userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
            android.util.Log.d("📋 BookingsHistory", "Combined bookings: ${userBookings.size}")
            userBookings.forEach {
                android.util.Log.d("📋 BookingsHistory", "  - Combined: ${it.bookingId} | ${it.locationName} | status: ${it.status}")
            }
        } else {
            android.util.Log.e("📋 BookingsHistory", "❌ User ID is empty!")
        }
        isLoading = false
        android.util.Log.d("📋 BookingsHistory", "=== FINISHED LOADING ===")
    }

    val currentTime = System.currentTimeMillis()
    val activeBookings = userBookings.filter {
        it.endTime > currentTime && it.status == "active"
    }
    val pastBookings = userBookings.filter {
        it.endTime <= currentTime || it.status in listOf("cancelled", "expired", "completed")
    }

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
                    text = "My Bookings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            IconButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        if (userId.isNotEmpty()) {
                            val firebaseBookings = BookingManager.getUserBookings(userId)
                            val localHistory = BookingHistory.history
                            val combinedMap = mutableMapOf<String, Booking>()
                            firebaseBookings.forEach { combinedMap[it.bookingId] = it }
                            localHistory.forEach { historyItem ->
                                val updatedBooking = historyItem.booking.copy(
                                    status = when (historyItem.status) {
                                        BookingStatus.ACTIVE -> "active"
                                        BookingStatus.CANCELLED -> "cancelled"
                                        BookingStatus.COMPLETED -> "completed"
                                    }
                                )
                                combinedMap[historyItem.booking.bookingId] = updatedBooking
                            }
                            userBookings = combinedMap.values.toList().sortedByDescending { it.startTime }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cardColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = textColor
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentGreen)
                }
            }
            userBookings.isEmpty() -> {
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
                            color = subtextColor
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                                isDarkTheme = isDarkTheme,
                                cardColor = cardColor,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                accentGreen = accentGreen,
                                onCancelClick = {
                                    bookingToCancel = activeBookings[index]
                                    showCancelDialog = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

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
                                isDarkTheme = isDarkTheme,
                                cardColor = cardColor,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                accentGreen = accentGreen,
                                isPast = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog && bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking?") },
            text = { Text("Are you sure you want to cancel this booking? A refund will be processed.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isCancelling = true
                            val result = BookingManager.cancelBooking(bookingToCancel!!.bookingId)
                            result.onSuccess {
                                BookingHistory.addBooking(bookingToCancel!!, BookingStatus.CANCELLED)
                                AlertsManager.showCancellationRefund(bookingToCancel!!)
                                userBookings = userBookings.map {
                                    if (it.bookingId == bookingToCancel!!.bookingId) {
                                        it.copy(status = "cancelled")
                                    } else it
                                }
                            }
                            isCancelling = false
                            showCancelDialog = false
                            bookingToCancel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isCancelling
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cancel Booking")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isDarkTheme: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    accentGreen: Color,
    isPast: Boolean = false,
    onCancelClick: () -> Unit = {}
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Garage,
                        contentDescription = null,
                        tint = if (isPast) subtextColor else accentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.locationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        booking.status == "cancelled" -> Color.Red.copy(alpha = 0.15f)
                        isPast -> subtextColor.copy(alpha = 0.15f)
                        else -> accentGreen.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            booking.status == "cancelled" -> Color.Red
                            isPast -> subtextColor
                            else -> accentGreen
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Space and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InfoRow(Icons.Default.LocationOn, "Space:", booking.spaceLabel, textColor, subtextColor)
                InfoRow(Icons.Default.CalendarToday, "Date:", formatDate(booking.startTime), textColor, subtextColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time - Full width for better readability
            InfoRow(Icons.Default.Schedule, "Time:", "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}", textColor, subtextColor)

            Spacer(modifier = Modifier.height(8.dp))

            // Duration
            InfoRow(Icons.Default.Timer, "Duration:", "${booking.totalHours}h", textColor, subtextColor)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: Rs.${String.format("%.2f", booking.totalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) subtextColor else accentGreen
                )

                if (!isPast && booking.status == "active") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = subtextColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = subtextColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private fun formatDate(timeMillis: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(Date(timeMillis))
}

private fun formatTime(timeMillis: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timeMillis))
}