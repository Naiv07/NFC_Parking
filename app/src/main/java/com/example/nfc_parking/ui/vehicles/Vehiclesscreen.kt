package com.example.nfc_parking.ui.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.Vehicle
import com.example.nfc_parking.data.VehicleManager
import com.example.nfc_parking.data.VehicleType
import kotlinx.coroutines.launch

/**
 * Helper to get icon and display label for each VehicleType
 */
fun VehicleType.icon(): ImageVector = when (this) {
    VehicleType.CAR -> Icons.Default.DirectionsCar
    VehicleType.BIKE -> Icons.Default.TwoWheeler
    VehicleType.EV -> Icons.Default.EvStation
    VehicleType.TRUCK -> Icons.Default.LocalShipping
}

fun VehicleType.label(): String = when (this) {
    VehicleType.CAR -> "Car"
    VehicleType.BIKE -> "Bike"
    VehicleType.EV -> "EV"
    VehicleType.TRUCK -> "Truck"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    // Colors
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val dangerColor = Color(0xFFFF4444)

    // State
    var vehicles by remember { mutableStateOf(listOf<Vehicle>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Vehicle?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load vehicles from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        VehicleManager.loadUserVehicles().onSuccess {
            vehicles = it
        }
        isLoading = false
    }

    // Add/Edit Dialog
    if (showAddDialog || editingVehicle != null) {
        VehicleFormDialog(
            isDarkTheme = isDarkTheme,
            accentColor = accentColor,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            vehicle = editingVehicle,
            onDismiss = {
                showAddDialog = false
                editingVehicle = null
            },
            onSave = { vehicle ->
                scope.launch {
                    if (editingVehicle != null) {
                        VehicleManager.updateVehicle(vehicle)
                    } else {
                        val newVehicle = vehicle.copy(
                            isDefault = vehicles.isEmpty()
                        )
                        VehicleManager.addVehicle(newVehicle)
                    }
                    // Reload
                    VehicleManager.loadUserVehicles().onSuccess {
                        vehicles = it
                    }
                    showAddDialog = false
                    editingVehicle = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = {
                Text("Remove Vehicle", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Text(
                    "Are you sure you want to remove ${showDeleteConfirm?.vehicleName?.ifEmpty { showDeleteConfirm?.licensePlate } ?: "this vehicle"}?",
                    color = subtextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showDeleteConfirm?.let { v ->
                                VehicleManager.deleteVehicle(v.id)
                                VehicleManager.loadUserVehicles().onSuccess {
                                    vehicles = it
                                }
                            }
                            showDeleteConfirm = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dangerColor)
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = subtextColor)
                }
            },
            containerColor = cardColor
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) listOf(
                        Color(0xFF0A0A0A), Color(0xFF0F0F0F), Color(0xFF121212)
                    ) else listOf(
                        Color(0xFFF8F9FA), Color(0xFFE8F4F8), Color(0xFFDCE9F0)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                }
                Text(
                    text = "My Vehicles",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add Vehicle", tint = accentColor)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (vehicles.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    accentColor.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No Vehicles Added",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add your vehicles to quickly book parking slots",
                            fontSize = 14.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add Vehicle",
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.Black else Color.White
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            isDarkTheme = isDarkTheme,
                            accentColor = accentColor,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            onEdit = { editingVehicle = vehicle },
                            onDelete = { showDeleteConfirm = vehicle },
                            onSetDefault = {
                                scope.launch {
                                    VehicleManager.setDefaultVehicle(vehicle.id)
                                    VehicleManager.loadUserVehicles().onSuccess {
                                        vehicles = it
                                    }
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Vehicle Type Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        vehicle.vehicleType.icon(),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vehicle.vehicleName.ifEmpty { vehicle.vehicleType.label() },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        if (vehicle.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Default",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vehicle.licensePlate.uppercase(),
                        fontSize = 14.sp,
                        color = subtextColor,
                        fontWeight = FontWeight.Medium
                    )
                    // Show model & color if available
                    if (vehicle.model.isNotEmpty() || vehicle.color.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = listOf(vehicle.model, vehicle.color)
                                .filter { it.isNotEmpty() }
                                .joinToString(" • "),
                            fontSize = 12.sp,
                            color = subtextColor.copy(alpha = 0.7f)
                        )
                    }
                }

                // More Options
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, "Options", tint = subtextColor)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (!vehicle.isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as Default") },
                                onClick = {
                                    expanded = false
                                    onSetDefault()
                                },
                                leadingIcon = { Icon(Icons.Default.Star, null, tint = accentColor) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = textColor) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove", color = Color(0xFFFF4444)) },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF4444)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle type badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6)
            ) {
                Text(
                    text = vehicle.vehicleType.label(),
                    fontSize = 12.sp,
                    color = subtextColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormDialog(
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit
) {
    var vehicleName by remember { mutableStateOf(vehicle?.vehicleName ?: "") }
    var licensePlate by remember { mutableStateOf(vehicle?.licensePlate ?: "") }
    var model by remember { mutableStateOf(vehicle?.model ?: "") }
    var color by remember { mutableStateOf(vehicle?.color ?: "") }
    var selectedType by remember { mutableStateOf(vehicle?.vehicleType ?: VehicleType.CAR) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (vehicle != null) "Edit Vehicle" else "Add Vehicle",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column {
                // Vehicle Type Selection
                Text("Vehicle Type", fontSize = 13.sp, color = subtextColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.2f)
                            else if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    type.icon(),
                                    contentDescription = null,
                                    tint = if (isSelected) accentColor else subtextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    type.label(),
                                    fontSize = 10.sp,
                                    color = if (isSelected) accentColor else subtextColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle Name
                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = { vehicleName = it; errorMsg = "" },
                    label = { Text("Vehicle Name", color = subtextColor) },
                    placeholder = { Text("e.g., Honda City", color = subtextColor.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // License Plate
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase(); errorMsg = "" },
                    label = { Text("License Plate *", color = subtextColor) },
                    placeholder = { Text("e.g., KA-01-AB-1234", color = subtextColor.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = accentColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Model & Color in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model", color = subtextColor) },
                        placeholder = { Text("e.g., 2020", color = subtextColor.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = accentColor
                        )
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color", color = subtextColor) },
                        placeholder = { Text("e.g., Red", color = subtextColor.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFD1D5DB),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = accentColor
                        )
                    )
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg, color = Color(0xFFFF4444), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (licensePlate.isBlank()) {
                        errorMsg = "License plate is required"
                        return@Button
                    }
                    onSave(
                        Vehicle(
                            id = vehicle?.id ?: "",
                            userId = vehicle?.userId ?: "",
                            vehicleName = vehicleName.trim(),
                            licensePlate = licensePlate.trim().uppercase(),
                            model = model.trim(),
                            color = color.trim(),
                            vehicleType = selectedType,
                            isDefault = vehicle?.isDefault ?: false,
                            createdAt = vehicle?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (vehicle != null) "Update" else "Add",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = subtextColor)
            }
        },
        containerColor = cardColor
    )
}