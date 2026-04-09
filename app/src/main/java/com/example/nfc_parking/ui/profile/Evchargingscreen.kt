package com.example.nfc_parking.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager

// Data classes
data class EVStation(
    val id: String,
    val name: String,
    val address: String,
    val distance: String,
    val rating: Float,
    val connectors: List<Connector>,
    val status: StationStatus,
    val pricePerKwh: Double,
    val openHours: String,
    val amenities: List<String> = emptyList()
)

data class Connector(
    val type: String,
    val power: String,
    val available: Int,
    val total: Int
)

enum class StationStatus(val label: String, val color: Color) {
    AVAILABLE("Available", Color(0xFF10B981)),
    BUSY("Busy", Color(0xFFF59E0B)),
    OFFLINE("Offline", Color(0xFFEF4444))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EVChargingScreen(
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // Colors
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val bgColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val evGreen = Color(0xFF10B981)

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Available", "Fast Charging", "Nearby")

    // Sample stations
    val stations = remember {
        listOf(
            EVStation(
                id = "EV001",
                name = "Indiranagar EV Hub",
                address = "100 Feet Road, Indiranagar, Bengaluru",
                distance = "2.3 km",
                rating = 4.5f,
                connectors = listOf(
                    Connector("CCS2", "50 kW", 2, 3),
                    Connector("Type 2", "22 kW", 1, 2)
                ),
                status = StationStatus.AVAILABLE,
                pricePerKwh = 14.0,
                openHours = "24/7",
                amenities = listOf("Cafe", "Restroom", "WiFi")
            ),
            EVStation(
                id = "EV002",
                name = "Forum Mall Charging Point",
                address = "Hosur Main Road, Koramangala",
                distance = "3.8 km",
                rating = 4.2f,
                connectors = listOf(
                    Connector("CCS2", "60 kW", 0, 2),
                    Connector("CHAdeMO", "50 kW", 1, 1),
                    Connector("Type 2", "7.4 kW", 3, 4)
                ),
                status = StationStatus.BUSY,
                pricePerKwh = 16.0,
                openHours = "6:00 AM - 11:00 PM",
                amenities = listOf("Shopping", "Food Court", "Restroom")
            ),
            EVStation(
                id = "EV003",
                name = "Electronic City Supercharger",
                address = "Hosur Road, Electronic City Phase 1",
                distance = "15.1 km",
                rating = 4.8f,
                connectors = listOf(
                    Connector("CCS2", "150 kW", 4, 6),
                    Connector("Type 2", "22 kW", 2, 2)
                ),
                status = StationStatus.AVAILABLE,
                pricePerKwh = 12.0,
                openHours = "24/7",
                amenities = listOf("Cafe", "Lounge", "WiFi", "Restroom")
            ),
            EVStation(
                id = "EV004",
                name = "Whitefield IT Park Charger",
                address = "ITPL Main Road, Whitefield",
                distance = "18.5 km",
                rating = 3.9f,
                connectors = listOf(
                    Connector("Type 2", "7.4 kW", 0, 3)
                ),
                status = StationStatus.OFFLINE,
                pricePerKwh = 15.0,
                openHours = "8:00 AM - 10:00 PM"
            )
        )
    }

    val filteredStations = when (selectedFilter) {
        "Available" -> stations.filter { it.status == StationStatus.AVAILABLE }
        "Fast Charging" -> stations.filter { station ->
            station.connectors.any { it.power.replace(" kW", "").toIntOrNull()?.let { p -> p >= 50 } == true }
        }
        "Nearby" -> stations.sortedBy { it.distance.replace(" km", "").toDoubleOrNull() ?: 0.0 }.take(3)
        else -> stations
    }

    // Station detail dialog
    var selectedStation by remember { mutableStateOf<EVStation?>(null) }

    if (selectedStation != null) {
        EVStationDetailDialog(
            station = selectedStation!!,
            isDarkTheme = isDarkTheme,
            accentColor = accentColor,
            evGreen = evGreen,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            onDismiss = { selectedStation = null }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EV Charging",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Map view */ }) {
                        Icon(Icons.Default.Map, "Map View", tint = accentColor)
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
            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EVStatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.EvStation,
                        value = "${stations.count { it.status == StationStatus.AVAILABLE }}",
                        label = "Available",
                        color = evGreen,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                    EVStatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt,
                        value = "${stations.flatMap { it.connectors }.sumOf { it.available }}",
                        label = "Free Ports",
                        color = Color(0xFF3B82F6),
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                    EVStatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.NearMe,
                        value = stations.firstOrNull()?.distance ?: "-",
                        label = "Nearest",
                        color = Color(0xFFF59E0B),
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    filter,
                                    fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                selectedLabelColor = accentColor,
                                containerColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6),
                                labelColor = subtextColor
                            ),
                            border = if (selectedFilter == filter)
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = accentColor
                                ) else null
                        )
                    }
                }
            }

            // Stations List
            if (filteredStations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EvStation,
                                null,
                                tint = subtextColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No stations found", fontSize = 16.sp, color = subtextColor)
                        }
                    }
                }
            } else {
                items(filteredStations, key = { it.id }) { station ->
                    EVStationCard(
                        station = station,
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        evGreen = evGreen,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        onClick = { selectedStation = station }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun EVStatMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
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
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(label, fontSize = 11.sp, color = subtextColor)
        }
    }
}

@Composable
fun EVStationCard(
    station: EVStation,
    isDarkTheme: Boolean,
    accentColor: Color,
    evGreen: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            station.status.color.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EvStation,
                        null,
                        tint = station.status.color,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        station.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        station.address,
                        fontSize = 12.sp,
                        color = subtextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = station.status.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        station.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = station.status.color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                station.connectors.forEach { connector ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                null,
                                tint = if (connector.available > 0) evGreen else Color(0xFFFF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${connector.type} ${connector.power}",
                                fontSize = 11.sp,
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${connector.available}/${connector.total}",
                                fontSize = 11.sp,
                                color = if (connector.available > 0) evGreen else Color(0xFFFF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = subtextColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(station.distance, fontSize = 12.sp, color = subtextColor)

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${station.rating}", fontSize = 12.sp, color = subtextColor)
                }

                Text(
                    "₹${station.pricePerKwh.toInt()}/kWh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun EVStationDetailDialog(
    station: EVStation,
    isDarkTheme: Boolean,
    accentColor: Color,
    evGreen: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(station.name, fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(station.address, fontSize = 13.sp, color = subtextColor)
            }
        },
        text = {
            Column {
                // Status & Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(station.status.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(station.status.label, fontSize = 13.sp, color = station.status.color, fontWeight = FontWeight.Bold)
                    }
                    Text(station.openHours, fontSize = 13.sp, color = subtextColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connectors Detail
                Text("Connectors", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(8.dp))
                station.connectors.forEach { connector ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${connector.type} (${connector.power})", fontSize = 13.sp, color = textColor)
                        }
                        Text(
                            "${connector.available} of ${connector.total} free",
                            fontSize = 13.sp,
                            color = if (connector.available > 0) evGreen else Color(0xFFFF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Price", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(
                        "₹${station.pricePerKwh.toInt()}/kWh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                // Amenities
                if (station.amenities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Amenities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        station.amenities.forEach { amenity ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    amenity,
                                    fontSize = 12.sp,
                                    color = subtextColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { /* Navigate to station */ },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Navigate",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = subtextColor)
            }
        },
        containerColor = cardColor
    )
}