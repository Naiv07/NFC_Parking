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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.*
import kotlinx.coroutines.launch

@Composable
fun VehiclesScreen(
    onBack: () -> Unit = {},
    onAddVehicle: () -> Unit = {},
    onEditVehicle: (Vehicle) -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val scope = rememberCoroutineScope()

    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Vehicle?>(null) }

    // Colors
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentGreen = Color(0xFF39FF14)

    // Load vehicles
    LaunchedEffect(Unit) {
        isLoading = true
        VehicleManager.loadUserVehicles().onSuccess {
            vehicles = VehicleManager.vehicles
        }
        isLoading = false
    }

    // Observe vehicle changes
    LaunchedEffect(VehicleManager.vehicles) {
        vehicles = VehicleManager.vehicles
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
                        text = "My Vehicles",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textColor
                    )
                }
            }

            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Hassle-Free Parking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = if (vehicles.isEmpty()) "No vehicles" else "${vehicles.size} ${if (vehicles.size == 1) "vehicle" else "vehicles"}",
                    fontSize = 14.sp,
                    color = subtextColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentGreen)
                }
            } else if (vehicles.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(accentGreen.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = accentGreen,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "No Vehicles Added",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add your vehicle to get started",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }
            } else {
                // Vehicles List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            cardColor = cardColor,
                            accentGreen = accentGreen,
                            onEdit = { onEditVehicle(vehicle) },
                            onDelete = { showDeleteDialog = vehicle },
                            onSetDefault = {
                                scope.launch {
                                    VehicleManager.setDefaultVehicle(vehicle.id)
                                    vehicles = VehicleManager.vehicles
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Navigation (Vehicle Type Selector)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    VehicleTypeButton(
                        icon = Icons.Default.DirectionsCar,
                        label = "Car",
                        isSelected = true,
                        accentGreen = accentGreen,
                        textColor = textColor,
                        onClick = { /* Filter */ }
                    )
                    VehicleTypeButton(
                        icon = Icons.Default.TwoWheeler,
                        label = "Bike",
                        isSelected = false,
                        accentGreen = accentGreen,
                        textColor = textColor,
                        onClick = { /* Filter */ }
                    )
                    VehicleTypeButton(
                        icon = Icons.Default.EvStation,
                        label = "EV",
                        isSelected = false,
                        accentGreen = accentGreen,
                        textColor = textColor,
                        onClick = { /* Filter */ }
                    )
                    VehicleTypeButton(
                        icon = Icons.Default.LocalShipping,
                        label = "Truck",
                        isSelected = false,
                        accentGreen = accentGreen,
                        textColor = textColor,
                        onClick = { /* Filter */ }
                    )
                }
            }

            // Add Vehicle Button
            Button(
                onClick = onAddVehicle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Car",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Vehicle?") },
                text = {
                    Text("Are you sure you want to delete ${showDeleteDialog!!.vehicleName}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                VehicleManager.deleteVehicle(showDeleteDialog!!.id)
                                vehicles = VehicleManager.vehicles
                                showDeleteDialog = null
                            }
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    accentGreen: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    val vehicleIcon = when (vehicle.vehicleType) {
        VehicleType.CAR -> Icons.Default.DirectionsCar
        VehicleType.BIKE -> Icons.Default.TwoWheeler
        VehicleType.EV -> Icons.Default.EvStation
        VehicleType.TRUCK -> Icons.Default.LocalShipping
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
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(accentGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vehicleIcon,
                            contentDescription = null,
                            tint = accentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = vehicle.vehicleName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = vehicle.licensePlate,
                            fontSize = 14.sp,
                            color = subtextColor
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = subtextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (vehicle.model.isNotEmpty()) {
                    Text(
                        text = "Model: ${vehicle.model}",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }
                if (vehicle.color.isNotEmpty()) {
                    Text(
                        text = "Color: ${vehicle.color}",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }
            }

            if (!vehicle.isDefault) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onSetDefault) {
                    Text(
                        text = "Set as Default",
                        color = accentGreen,
                        fontSize = 12.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✓ Default Vehicle",
                        color = accentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleTypeButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentGreen: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isSelected) accentGreen else Color.Transparent,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else textColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) accentGreen else textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}