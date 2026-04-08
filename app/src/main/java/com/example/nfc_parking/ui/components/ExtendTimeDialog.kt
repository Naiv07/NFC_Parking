package com.example.nfc_parking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.ThemeManager
import kotlin.math.roundToInt

@Composable
fun ExtendTimeDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirm: (additionalHours: Int) -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    var additionalHours by remember { mutableStateOf(1) }

    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentGreen = Color(0xFF39FF14)

    val pricePerHour = booking.pricePerHour
    val additionalCost = pricePerHour * additionalHours

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extend Parking Time",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Current booking info
                Text(
                    text = booking.locationName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = "Space: ${booking.spaceLabel}",
                    fontSize = 14.sp,
                    color = subtextColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hours slider
                Text(
                    text = "Add ${additionalHours}h",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = additionalHours.toFloat(),
                    onValueChange = { additionalHours = it.roundToInt() },
                    valueRange = 1f..6f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = accentGreen,
                        activeTrackColor = accentGreen,
                        inactiveTrackColor = subtextColor.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1h", fontSize = 12.sp, color = subtextColor)
                    Text("6h", fontSize = 12.sp, color = subtextColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cost breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Additional cost:",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Rs.${String.format("%.2f", additionalCost)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentGreen
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirm(additionalHours) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Extend Time",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}