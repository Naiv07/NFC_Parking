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
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.data.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    // Real-time refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
                userBookings = BookingManager.getUserBookings(userId)
            }
            isLoading = false
            kotlinx.coroutines.delay(5000)
        }
    }

    val currentTime = System.currentTimeMillis()
    val activeBookings = userBookings.filter { it.endTime > currentTime && it.status == "active" }
    val pastBookings = userBookings.filter { it.endTime <= currentTime || it.status == "cancelled" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.background(cardColor, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                }

                Text(
                    "My Bookings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            if (userId.isNotEmpty()) {
                                userBookings = BookingManager.getUserBookings(userId)
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.background(cardColor, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = textColor)
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (userBookings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, tint = subtextColor, modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No Bookings Yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(Modifier.height(8.dp))
                        Text("Your parking history will appear here", fontSize = 14.sp, color = subtextColor, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeBookings.isNotEmpty()) {
                        item {
                            Text("Active (${activeBookings.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        items(activeBookings.size) { index ->
                            BookingCard(
                                activeBookings[index], true, cardColor, textColor, subtextColor, accentColor,
                                onCancel = { bookingToCancel = activeBookings[index]; showCancelDialog = true }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    if (pastBookings.isNotEmpty()) {
                        item {
                            Text("Past (${pastBookings.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        items(pastBookings.size) { index ->
                            BookingCard(pastBookings[index], false, cardColor, textColor, subtextColor, subtextColor)
                        }
                    }
                }
            }
        }
    }

    if (showCancelDialog && bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = cardColor,
            title = { Text("Cancel Booking?", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Column {
                    Text("Are you sure you want to cancel this booking?", color = textColor)
                    Spacer(Modifier.height(12.dp))
                    Text("Location: ${bookingToCancel!!.locationName}", fontSize = 14.sp, color = subtextColor)
                    Text("Space: ${bookingToCancel!!.spaceLabel}", fontSize = 14.sp, color = subtextColor)
                    Text("Amount: ₹${String.format("%.2f", bookingToCancel!!.totalPrice)}", fontSize = 14.sp, color = subtextColor)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isCancelling = true
                            BookingManager.cancelPendingBooking(bookingToCancel!!.bookingId).onSuccess {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                if (userId.isNotEmpty()) {
                                    userBookings = BookingManager.getUserBookings(userId)
                                }
                            }
                            isCancelling = false
                            showCancelDialog = false
                        }
                    },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Cancel Booking", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }, enabled = !isCancelling) {
                    Text("Keep Booking", color = textColor)
                }
            }
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    isActive: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    accentColor: Color,
    onCancel: () -> Unit = {}
) {
    fun formatDate(t: Long) = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(t))
    fun formatTime(t: Long) = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(t))

    val statusColor = when (booking.status) {
        "active" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        "cancelled" -> Color(0xFFEF4444)
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

                // ✅ FIXED: Added "shape =" and "color =" parameter names
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

            BookingRow(
                icon = Icons.Default.LocationOn,
                label = "Space",
                value = booking.spaceLabel,
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingRow(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = formatDate(booking.startTime),
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingRow(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                textColor = textColor,
                subtextColor = subtextColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingRow(
                icon = Icons.Default.Timer,
                label = "Duration",
                value = "${booking.totalHours} ${if (booking.totalHours == 1) "hour" else "hours"}",
                textColor = textColor,
                subtextColor = subtextColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = subtextColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

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
                        text = "₹${String.format("%.2f", booking.totalPrice)}",
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
fun BookingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color,
    subtextColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = subtextColor, modifier = Modifier.size(16.dp))
        Text("$label:", fontSize = 14.sp, color = subtextColor, modifier = Modifier.width(70.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}