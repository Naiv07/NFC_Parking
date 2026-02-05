package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ParkingTicketScreen(
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    // Generate unique booking ID
    val bookingId = remember { "BID-${System.currentTimeMillis().toString().takeLast(12)}" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Top Bar
            item {
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
                        text = "Parking Ticket",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            // Paid Badge
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF4CAF50)
                    ) {
                        Text(
                            text = "Paid",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Main Ticket Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Parking Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(subtextColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Garage,
                                    contentDescription = "Parking",
                                    tint = subtextColor,
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Grand Central Garage",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "123 Main St, NY",
                                    fontSize = 14.sp,
                                    color = subtextColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketDashedDivider(color = subtextColor.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(20.dp))

                        // Date & Time
                        TicketInfoItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Date & Time",
                            value = "Tue, Feb 02, 2026 | 2:30 PM - 6:00 PM",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment Method
                        TicketInfoItem(
                            icon = Icons.Default.Payment,
                            label = "Paid through UPI",
                            value = "",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketDashedDivider(color = subtextColor.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(20.dp))

                        // Booking ID
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Booking ID",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = subtextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bookingId,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // QR Code Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    tint = Color.Black,
                                    modifier = Modifier.size(120.dp)
                                )
                                Text(
                                    text = bookingId,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Scan for Entry/Exit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TicketActionButton(
                        icon = Icons.Default.Map,
                        label = "Get Directions",
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        accentColor = accentColor
                    )

                    TicketActionButton(
                        icon = Icons.Default.Share,
                        label = "Share Receipt",
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        accentColor = accentColor
                    )

                    TicketActionButton(
                        icon = Icons.Default.Chat,
                        label = "Support",
                        modifier = Modifier.weight(1f),
                        cardColor = cardColor,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
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
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = subtextColor
                )
            }
        }
    }
}

@Composable
private fun TicketActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    accentColor: Color
) {
    Button(
        onClick = { },
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TicketDashedDivider(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f), 0f
            )
        )
    }
}