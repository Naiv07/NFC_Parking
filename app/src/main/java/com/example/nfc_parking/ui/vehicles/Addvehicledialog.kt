package com.example.nfc_parking.ui.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.nfc_parking.data.ThemeManager
import java.util.UUID

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAddVehicle: (Vehicle) -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    var vehicleName by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VehicleType.CAR) }
    var showError by remember { mutableStateOf(false) }

    // 🎨 Color Scheme
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Add New Vehicle",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Vehicle Name Input
                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = {
                        vehicleName = it
                        showError = false
                    },
                    label = { Text("Vehicle Name", color = subtextColor) },
                    placeholder = { Text("e.g., My Tesla", color = subtextColor.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor
                    ),
                    singleLine = true,
                    isError = showError && vehicleName.isEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle Model Input
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = {
                        vehicleModel = it
                        showError = false
                    },
                    label = { Text("Model", color = subtextColor) },
                    placeholder = { Text("e.g., Model 3", color = subtextColor.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor
                    ),
                    singleLine = true,
                    isError = showError && vehicleModel.isEmpty()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Vehicle Type Selection
                Text(
                    text = "Vehicle Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleTypeChip(
                        label = "Car",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = selectedType == VehicleType.CAR,
                        accentColor = accentColor,
                        cardColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                        textColor = textColor,
                        onClick = { selectedType = VehicleType.CAR }
                    )

                    VehicleTypeChip(
                        label = "Bike",
                        icon = Icons.Default.TwoWheeler,
                        isSelected = selectedType == VehicleType.BIKE,
                        accentColor = accentColor,
                        cardColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                        textColor = textColor,
                        onClick = { selectedType = VehicleType.BIKE }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleTypeChip(
                        label = "Truck",
                        icon = Icons.Default.LocalShipping,
                        isSelected = selectedType == VehicleType.TRUCK,
                        accentColor = accentColor,
                        cardColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                        textColor = textColor,
                        onClick = { selectedType = VehicleType.TRUCK }
                    )

                    VehicleTypeChip(
                        label = "EV",
                        icon = Icons.Default.EvStation,
                        isSelected = selectedType == VehicleType.EV,
                        accentColor = accentColor,
                        cardColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                        textColor = textColor,
                        onClick = { selectedType = VehicleType.EV }
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please fill in all fields",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
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
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    // Add Button
                    Button(
                        onClick = {
                            if (vehicleName.isNotEmpty() && vehicleModel.isNotEmpty()) {
                                val newVehicle = Vehicle(
                                    id = UUID.randomUUID().toString(),
                                    name = vehicleName,
                                    model = vehicleModel,
                                    type = selectedType
                                )
                                onAddVehicle(newVehicle)
                                onDismiss()
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (isDarkTheme) Color.Black else Color.White
                        )
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.VehicleTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor else cardColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.2f)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else textColor
            )
        }
    }
}