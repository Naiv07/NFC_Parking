package com.example.nfc_parking.ui.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nfc_parking.data.*
import kotlinx.coroutines.launch

@Composable
fun AddVehicleDialog(
    vehicleToEdit: Vehicle? = null,
    onDismiss: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    // State
    var selectedType by remember { mutableStateOf(vehicleToEdit?.vehicleType ?: VehicleType.CAR) }
    var vehicleName by remember { mutableStateOf(vehicleToEdit?.vehicleName ?: "") }
    var model by remember { mutableStateOf(vehicleToEdit?.model ?: "") }
    var licensePlate by remember { mutableStateOf(vehicleToEdit?.licensePlate ?: "") }
    var color by remember { mutableStateOf(vehicleToEdit?.color ?: "") }
    var isDefault by remember { mutableStateOf(vehicleToEdit?.isDefault ?: false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Colors
    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentGreen = Color(0xFF39FF14)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (vehicleToEdit != null) "Edit Vehicle" else "Add Vehicle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textColor
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Vehicle Type Selection
                    Text(
                        text = "Vehicle Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VehicleTypeChip(
                            type = VehicleType.CAR,
                            icon = Icons.Default.DirectionsCar,
                            label = "Car",
                            isSelected = selectedType == VehicleType.CAR,
                            onClick = { selectedType = VehicleType.CAR },
                            accentGreen = accentGreen,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        VehicleTypeChip(
                            type = VehicleType.BIKE,
                            icon = Icons.Default.TwoWheeler,
                            label = "Bike",
                            isSelected = selectedType == VehicleType.BIKE,
                            onClick = { selectedType = VehicleType.BIKE },
                            accentGreen = accentGreen,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VehicleTypeChip(
                            type = VehicleType.EV,
                            icon = Icons.Default.EvStation,
                            label = "EV",
                            isSelected = selectedType == VehicleType.EV,
                            onClick = { selectedType = VehicleType.EV },
                            accentGreen = accentGreen,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        VehicleTypeChip(
                            type = VehicleType.TRUCK,
                            icon = Icons.Default.LocalShipping,
                            label = "Truck",
                            isSelected = selectedType == VehicleType.TRUCK,
                            onClick = { selectedType = VehicleType.TRUCK },
                            accentGreen = accentGreen,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Vehicle Name
                    OutlinedTextField(
                        value = vehicleName,
                        onValueChange = { vehicleName = it },
                        label = { Text("Vehicle Name") },
                        placeholder = { Text("e.g., Honda City") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreen,
                            cursorColor = accentGreen
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model/Year") },
                        placeholder = { Text("e.g., 2020") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreen,
                            cursorColor = accentGreen
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // License Plate
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it.uppercase() },
                        label = { Text("License Plate") },
                        placeholder = { Text("e.g., KA-01-AB-1234") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreen,
                            cursorColor = accentGreen
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color (optional)") },
                        placeholder = { Text("e.g., Red") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreen,
                            cursorColor = accentGreen
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Set as Default
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDefault = !isDefault }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set as Default Vehicle",
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Switch(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Bottom Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            when {
                                vehicleName.isBlank() -> errorMessage = "Vehicle name is required"
                                licensePlate.isBlank() -> errorMessage = "License plate is required"
                                else -> {
                                    isSaving = true
                                    errorMessage = null
                                    scope.launch {
                                        val vehicle = Vehicle(
                                            id = vehicleToEdit?.id ?: "",
                                            vehicleType = selectedType,
                                            vehicleName = vehicleName,
                                            model = model,
                                            licensePlate = licensePlate,
                                            color = color,
                                            isDefault = isDefault
                                        )

                                        val result = if (vehicleToEdit != null) {
                                            VehicleManager.updateVehicle(vehicle)
                                        } else {
                                            VehicleManager.addVehicle(vehicle)
                                        }

                                        result.onSuccess {
                                            onSaved()
                                        }.onFailure { error ->
                                            errorMessage = error.message
                                            isSaving = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentGreen
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (vehicleToEdit != null) "Update" else "Add",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleTypeChip(
    type: VehicleType,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentGreen: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentGreen.copy(alpha = 0.15f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentGreen else textColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentGreen else textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentGreen else textColor
            )
        }
    }
}