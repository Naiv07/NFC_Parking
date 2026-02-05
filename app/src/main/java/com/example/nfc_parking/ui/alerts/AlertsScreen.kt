package com.example.nfc_parking.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.AlertManager
import com.example.nfc_parking.data.Alert
import com.example.nfc_parking.data.AlertUrgency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBack: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // Color scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    // Get alerts from AlertsManager - this will auto-recompose when alerts change
    val alerts = AlertManager.alerts

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alerts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (alerts.isNotEmpty()) {
                        TextButton(
                            onClick = { AlertManager.clearAllAlerts() }
                        ) {
                            Text(
                                text = "Clear All",
                                color = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            if (alerts.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Alerts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }
            } else {
                // Alerts list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = alerts,
                        key = { alert -> alert.id }
                    ) { alert ->
                        AlertCard(
                            alert = alert,
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            cardColor = cardColor,
                            onDismiss = {
                                AlertManager.dismissAlert(alert.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: Alert,
    isDarkTheme: Boolean,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    onDismiss: () -> Unit
) {
    val urgencyColor = when (alert.urgency) {
        AlertUrgency.HIGH -> Color(0xFFEF4444)
        AlertUrgency.MEDIUM -> Color(0xFFF59E0B)
        AlertUrgency.LOW -> Color(0xFF10B981)
    }

    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    fun formatTime(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left urgency indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(urgencyColor)
            )

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (alert.urgency) {
                                AlertUrgency.HIGH -> Icons.Default.Warning
                                AlertUrgency.MEDIUM -> Icons.Default.Schedule
                                AlertUrgency.LOW -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = urgencyColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alert.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 2
                        )
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

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.location,
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Booking details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Spot",
                            fontSize = 10.sp,
                            color = subtextColor
                        )
                        Text(
                            text = alert.spotNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Time Left",
                            fontSize = 10.sp,
                            color = subtextColor
                        )
                        Text(
                            text = alert.timeRemaining,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = urgencyColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Ends At",
                            fontSize = 10.sp,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Show amount for refund alerts, otherwise show action buttons
                if (alert.title.contains("Refund") && alert.amount != null) {
                    // Refund info
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Refund Amount",
                                    fontSize = 12.sp,
                                    color = subtextColor
                                )
                            }
                            Text(
                                text = "Rs.${String.format("%.2f", alert.amount)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                } else {
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Handle extend time */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = accentColor
                            )
                        ) {
                            Text(
                                text = "Extend Time",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { /* Handle view booking */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            )
                        ) {
                            Text(
                                text = "View Booking",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}