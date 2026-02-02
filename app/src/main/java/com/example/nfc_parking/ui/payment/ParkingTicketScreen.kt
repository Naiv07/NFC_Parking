package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ParkingTicketScreen(
    booking: Booking,
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    // Format time helper
    fun formatDateTime(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy | h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    fun formatTime(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                    text = "Parking Ticket",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                    text = booking.locationName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = booking.spaceLabel,
                                    fontSize = 14.sp,
                                    color = subtextColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketDashedDivider(color = subtextColor.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketInfoItem(
                            icon = Icons.Default.CalendarToday,
                            label = "Date & Time",
                            value = "${formatDateTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TicketInfoItem(
                            icon = Icons.Default.Timer,
                            label = "Duration",
                            value = "${booking.totalHours} ${if (booking.totalHours == 1) "hour" else "hours"}",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TicketInfoItem(
                            icon = Icons.Default.Payment,
                            label = "Paid through UPI",
                            value = "₹${String.format("%.2f", booking.totalPrice)}",
                            textColor = textColor,
                            subtextColor = subtextColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TicketDashedDivider(color = subtextColor.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(20.dp))

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
                                text = booking.bookingId,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "QR CODE\n${booking.bookingId}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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