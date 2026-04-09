package com.example.nfc_parking.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptsScreen(
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // Colors
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val bgColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Completed", "Cancelled", "Active")

    // Get booking history
    val allHistory = remember { BookingHistory.history }

    val filteredHistory = when (selectedTab) {
        1 -> allHistory.filter { it.status == BookingStatus.COMPLETED }
        2 -> allHistory.filter { it.status == BookingStatus.CANCELLED }
        3 -> allHistory.filter { it.status == BookingStatus.ACTIVE }
        else -> allHistory
    }.sortedByDescending { it.booking.createdAt }

    // Saved payment methods
    val savedCards = remember { PaymentManager.savedCards.toList() }
    val savedUpiIds = remember { PaymentManager.savedUpiIds.toList() }

    // Stats
    val totalSpent = allHistory
        .filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.ACTIVE }
        .sumOf { it.booking.totalPrice }
    val totalRefunded = allHistory
        .filter { it.status == BookingStatus.CANCELLED }
        .sumOf { it.booking.totalPrice }

    // Receipt detail dialog
    var selectedBooking by remember { mutableStateOf<BookingHistoryItem?>(null) }

    if (selectedBooking != null) {
        ReceiptDialog(
            item = selectedBooking!!,
            isDarkTheme = isDarkTheme,
            accentColor = accentColor,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            onDismiss = { selectedBooking = null }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Payment & Receipts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Payment,
                        label = "Total Spent",
                        value = "₹${String.format("%.2f", totalSpent)}",
                        color = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                    PaymentStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Replay,
                        label = "Refunded",
                        value = "₹${String.format("%.2f", totalRefunded)}",
                        color = Color(0xFF10B981),
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                    PaymentStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Receipt,
                        label = "Bookings",
                        value = "${allHistory.size}",
                        color = Color(0xFF3B82F6),
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                }
            }

            // Saved Payment Methods
            if (savedCards.isNotEmpty() || savedUpiIds.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Saved Payment Methods",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                items(savedCards) { card ->
                    SavedCardItem(
                        card = card,
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                }

                items(savedUpiIds) { upi ->
                    SavedUpiItem(
                        upi = upi,
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                }
            }

            // Transaction History Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Transaction History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = accentColor,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = accentColor
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) accentColor else subtextColor
                                )
                            }
                        )
                    }
                }
            }

            // Empty State or Transaction Items
            if (filteredHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = subtextColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No transactions yet",
                                fontSize = 16.sp,
                                color = subtextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your booking transactions will appear here",
                                fontSize = 13.sp,
                                color = subtextColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(filteredHistory, key = { it.booking.bookingId }) { historyItem ->
                    TransactionCard(
                        item = historyItem,
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        onClick = { selectedBooking = historyItem }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ──────────────────────────────────────────────
// Stat Card
// ──────────────────────────────────────────────
@Composable
fun PaymentStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(label, fontSize = 11.sp, color = subtextColor)
        }
    }
}

// ──────────────────────────────────────────────
// Transaction Card
// ──────────────────────────────────────────────
@Composable
fun TransactionCard(
    item: BookingHistoryItem,
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    val booking = item.booking

    val statusIcon: ImageVector
    val statusColor: Color
    val amountPrefix: String
    val statusLabel: String

    when (item.status) {
        BookingStatus.ACTIVE -> {
            statusIcon = Icons.Default.LocalParking
            statusColor = accentColor
            amountPrefix = "- "
            statusLabel = "Active"
        }
        BookingStatus.COMPLETED -> {
            statusIcon = Icons.Default.CheckCircle
            statusColor = Color(0xFF10B981)
            amountPrefix = "- "
            statusLabel = "Paid"
        }
        BookingStatus.CANCELLED -> {
            statusIcon = Icons.Default.Cancel
            statusColor = Color(0xFFF59E0B)
            amountPrefix = "+ "
            statusLabel = "Refunded"
        }
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    booking.locationName.ifEmpty { "Parking Booking" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${booking.spaceLabel} • ${booking.totalHours}h",
                    fontSize = 12.sp,
                    color = subtextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateFormat.format(Date(booking.createdAt)),
                    fontSize = 11.sp,
                    color = subtextColor.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${amountPrefix}₹${String.format("%.2f", booking.totalPrice)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.status == BookingStatus.CANCELLED) Color(0xFF10B981) else Color(0xFFFF4444)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusLabel,
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Saved Card Item
// ──────────────────────────────────────────────
@Composable
fun SavedCardItem(
    card: SavedCard,
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Brush.linearGradient(card.gradientColors),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        card.cardholderName.ifEmpty { card.bankName.ifEmpty { "Card" } },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    if (card.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Default",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    "•••• ${card.cardNumber.takeLast(4)}",
                    fontSize = 12.sp,
                    color = subtextColor
                )
            }
            Text(card.expiryDate, fontSize = 12.sp, color = subtextColor)
        }
    }
}

// ──────────────────────────────────────────────
// Saved UPI Item
// ──────────────────────────────────────────────
@Composable
fun SavedUpiItem(
    upi: SavedUpiId,
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Color(0xFF4285F4).copy(alpha = 0.15f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF4285F4), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("UPI", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    if (upi.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Default",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(upi.upiId, fontSize = 12.sp, color = subtextColor)
            }
        }
    }
}

// ──────────────────────────────────────────────
// Receipt Dialog
// ──────────────────────────────────────────────
@Composable
fun ReceiptDialog(
    item: BookingHistoryItem,
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onDismiss: () -> Unit
) {
    val booking = item.booking
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dividerColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFE5E7EB)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, null, tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Receipt", fontWeight = FontWeight.Bold, color = textColor)
            }
        },
        text = {
            Column {
                ReceiptInfoRow("Booking ID", booking.bookingId, textColor, subtextColor)
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow("Location", booking.locationName, textColor, subtextColor)
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow("Space", booking.spaceLabel, textColor, subtextColor)
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow("Date", dateFormat.format(Date(booking.startTime)), textColor, subtextColor)
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow(
                    "Time",
                    "${timeFormat.format(Date(booking.startTime))} - ${timeFormat.format(Date(booking.endTime))}",
                    textColor, subtextColor
                )
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow(
                    "Duration",
                    "${booking.totalHours} hour${if (booking.totalHours > 1) "s" else ""}",
                    textColor, subtextColor
                )
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow("Rate", "₹${String.format("%.2f", booking.pricePerHour)}/hr", textColor, subtextColor)
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                ReceiptInfoRow(
                    "Status",
                    when (item.status) {
                        BookingStatus.ACTIVE -> "Active"
                        BookingStatus.COMPLETED -> "Completed"
                        BookingStatus.CANCELLED -> "Cancelled (Refunded)"
                    },
                    textColor, subtextColor
                )

                if (booking.paymentMethod != null) {
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    ReceiptInfoRow("Payment", booking.paymentMethod, textColor, subtextColor)
                }

                if (!booking.vehiclePlate.isNullOrEmpty()) {
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    ReceiptInfoRow(
                        "Vehicle",
                        "${booking.vehiclePlate} ${booking.vehicleModel ?: ""}".trim(),
                        textColor, subtextColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(
                        "₹${String.format("%.2f", booking.totalPrice)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Close",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White
                )
            }
        },
        containerColor = cardColor
    )
}

@Composable
fun ReceiptInfoRow(label: String, value: String, textColor: Color, subtextColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = subtextColor)
        Text(
            value,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 200.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}