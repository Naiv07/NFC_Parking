package com.example.nfc_parking.ui.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.nfc_parking.data.*
import kotlinx.coroutines.launch

@Composable
fun AddEditVehicleScreen(
    vehicleToEdit: Vehicle? = null,
    onBack: () -> Unit = {},
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
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentGreen = Color(0xFF39FF14)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (vehicleToEdit != null) "Edit Vehicle" else "Add Vehicle",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Vehicle Type Selection
            Text(
                text = "Vehicle Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VehicleTypeCard(
                    type = VehicleType.CAR,
                    icon = Icons.Default.DirectionsCar,
                    label = "Car",
                    isSelected = selectedType == VehicleType.CAR,
                    onClick = { selectedType = VehicleType.CAR },
                    accentGreen = accentGreen,
                    cardColor = cardColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                VehicleTypeCard(
                    type = VehicleType.BIKE,
                    icon = Icons.Default.TwoWheeler,
                    label = "Bike",
                    isSelected = selectedType == VehicleType.BIKE,
                    onClick = { selectedType = VehicleType.BIKE },
                    accentGreen = accentGreen,
                    cardColor = cardColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VehicleTypeCard(
                    type = VehicleType.EV,
                    icon = Icons.Default.EvStation,
                    label = "EV",
                    isSelected = selectedType == VehicleType.EV,
                    onClick = { selectedType = VehicleType.EV },
                    accentGreen = accentGreen,
                    cardColor = cardColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
                VehicleTypeCard(
                    type = VehicleType.TRUCK,
                    icon = Icons.Default.LocalShipping,
                    label = "Truck",
                    isSelected = selectedType == VehicleType.TRUCK,
                    onClick = { selectedType = VehicleType.TRUCK },
                    accentGreen = accentGreen,
                    cardColor = cardColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Details
            Text(
                text = "Vehicle Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle Name
            OutlinedTextField(
                value = vehicleName,
                onValueChange = { vehicleName = it },
                label = { Text("Vehicle Name (e.g., Honda City)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGreen,
                    focusedLabelColor = accentGreen,
                    unfocusedBorderColor = subtextColor,
                    unfocusedLabelColor = subtextColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model/Year (e.g., 2020)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGreen,
                    focusedLabelColor = accentGreen,
                    unfocusedBorderColor = subtextColor,
                    unfocusedLabelColor = subtextColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // License Plate
            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text("License Plate (e.g., KA-01-AB-1234)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGreen,
                    focusedLabelColor = accentGreen,
                    unfocusedBorderColor = subtextColor,
                    unfocusedLabelColor = subtextColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGreen,
                    focusedLabelColor = accentGreen,
                    unfocusedBorderColor = subtextColor,
                    unfocusedLabelColor = subtextColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
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
                Column {
                    Text(
                        text = "Set as Default Vehicle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Text(
                        text = "Use this vehicle for future bookings",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = accentGreen,
                        uncheckedThumbColor = subtextColor,
                        uncheckedTrackColor = cardColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Save Button
            Button(
                onClick = {
                    // Validate
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
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentGreen,
                    disabledContainerColor = accentGreen.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (vehicleToEdit != null) "Update Vehicle" else "Add Vehicle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VehicleTypeCard(
    type: VehicleType,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentGreen: Color,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentGreen.copy(alpha = 0.15f) else cardColor
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, accentGreen)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentGreen else textColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accentGreen else textColor
            )
        }
    }
}