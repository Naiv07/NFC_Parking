package com.example.nfc_parking.ui.parkingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.SavedLocationsManager
import com.example.nfc_parking.ui.home.ParkingLocation

// Vehicle slot availability data
data class VehicleSlotInfo(
    val vehicleType: String,
    val icon: ImageVector,
    val availableSpots: Int,
    val totalSpots: Int,
    val description: String,
    val status: SlotStatus
)

enum class SlotStatus {
    AVAILABLE,    // Many spots available
    LIMITED,      // Few spots left
    FULL          // No spots available
}

@Composable
fun ParkingLocationDetailScreen(
    location: ParkingLocation,
    onBack: () -> Unit,
    onNavigate: () -> Unit = {},
    onBookNow: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val isSaved = SavedLocationsManager.isSaved(location)

    var selectedVehicleType by remember { mutableStateOf("Compact Cars") }

    // 🎨 Color Scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)

    // Generate dynamic vehicle slot availability (different for each location)
    val vehicleSlots = remember(location.id) {
        generateVehicleSlots(location.id)
    }

    // Calculate total available spots across all vehicle types
    val totalAvailableSpots = vehicleSlots.sumOf { it.availableSpots }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 🎨 HEADER IMAGE SECTION
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    // Background with subtle gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        surfaceColor,
                                        surfaceColor.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    ) {
                        // Large parking icon placeholder
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = location.accentColor.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Top bar with back and more buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(cardColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }

                        // More options button
                        IconButton(
                            onClick = { /* TODO: Show options */ },
                            modifier = Modifier
                                .background(cardColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = textColor
                            )
                        }
                    }
                }
            }

            // 📍 LOCATION INFO CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-40).dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = location.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                text = location.address,
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4.5",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < 4) Icons.Default.Star else Icons.Default.StarHalf,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(126 reviews)",
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Price
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = location.pricePerHour,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/hour",
                                fontSize = 16.sp,
                                color = subtextColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Open status
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open 24 Hours",
                                fontSize = 14.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 🚗 VEHICLE TYPE AVAILABILITY
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Available Spots",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$totalAvailableSpots spots available",
                        fontSize = 14.sp,
                        color = subtextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vehicle slot cards
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vehicleSlots.size) { index ->
                            val slot = vehicleSlots[index]
                            VehicleSlotCard(
                                slotInfo = slot,
                                isSelected = selectedVehicleType == slot.vehicleType,
                                cardColor = cardColor,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                surfaceColor = surfaceColor,
                                accentColor = location.accentColor,
                                onClick = { selectedVehicleType = slot.vehicleType }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 📊 BUSY TIMES INFO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Most busy on Friday evenings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simple bar chart placeholder
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Simple bar chart representation
                            val busyLevels = listOf(0.4f, 0.3f, 0.5f, 0.7f, 0.9f, 0.6f, 0.5f)
                            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                            busyLevels.forEachIndexed { index, level ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height((60 * level).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (index == 4) location.accentColor
                                                else location.accentColor.copy(alpha = 0.3f)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = days[index],
                                        fontSize = 10.sp,
                                        color = if (index == 4) textColor else subtextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 🏢 FACILITIES SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Facilities",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FacilityItem(
                                icon = Icons.Default.Security,
                                text = "24/7 Security",
                                accentColor = location.accentColor,
                                textColor = textColor
                            )
                            FacilityItem(
                                icon = Icons.Default.Videocam,
                                text = "CCTV Surveillance",
                                accentColor = location.accentColor,
                                textColor = textColor
                            )
                            FacilityItem(
                                icon = Icons.Default.Accessible,
                                text = "Wheelchair Accessible",
                                accentColor = location.accentColor,
                                textColor = textColor
                            )
                            FacilityItem(
                                icon = Icons.Default.WbSunny,
                                text = "Covered Parking",
                                accentColor = location.accentColor,
                                textColor = textColor
                            )
                            FacilityItem(
                                icon = Icons.Default.EvStation,
                                text = "EV Charging Available",
                                accentColor = location.accentColor,
                                textColor = textColor
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 📍 QUICK INFO
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickInfoCard(
                        icon = Icons.Default.Place,
                        label = "Distance",
                        value = location.distance,
                        accentColor = location.accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        modifier = Modifier.weight(1f)
                    )

                    QuickInfoCard(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = location.duration,
                        accentColor = location.accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 💳 BOTTOM BOOKING BAR
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = cardColor,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = location.accentColor,
                        contentColor = if (isDarkTheme) Color.Black else Color.White
                    )
                ) {
                    Text(
                        text = "Book a Spot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Generate different vehicle slot availability based on location ID
fun generateVehicleSlots(locationId: String): List<VehicleSlotInfo> {
    val seed = locationId.hashCode()
    val random = kotlin.random.Random(seed)

    return listOf(
        VehicleSlotInfo(
            vehicleType = "Compact Cars",
            icon = Icons.Default.DirectionsCar,
            availableSpots = random.nextInt(10, 35),
            totalSpots = random.nextInt(35, 50),
            description = "Easy to park",
            status = SlotStatus.AVAILABLE
        ),
        VehicleSlotInfo(
            vehicleType = "Sedan / SUV",
            icon = Icons.Default.DirectionsCar,
            availableSpots = random.nextInt(5, 20),
            totalSpots = random.nextInt(20, 40),
            description = "Mid-size zone",
            status = if (random.nextBoolean()) SlotStatus.AVAILABLE else SlotStatus.LIMITED
        ),
        VehicleSlotInfo(
            vehicleType = "Large Vehicles",
            icon = Icons.Default.LocalShipping,
            availableSpots = random.nextInt(0, 12),
            totalSpots = random.nextInt(12, 25),
            description = "Spacious area",
            status = when {
                random.nextInt(10) > 7 -> SlotStatus.FULL
                random.nextInt(10) > 4 -> SlotStatus.LIMITED
                else -> SlotStatus.AVAILABLE
            }
        ),
        VehicleSlotInfo(
            vehicleType = "Motorcycles",
            icon = Icons.Default.TwoWheeler,
            availableSpots = random.nextInt(15, 45),
            totalSpots = random.nextInt(45, 60),
            description = "Two-wheeler zone",
            status = SlotStatus.AVAILABLE
        )
    )
}

@Composable
fun VehicleSlotCard(
    slotInfo: VehicleSlotInfo,
    isSelected: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val statusColor = when (slotInfo.status) {
        SlotStatus.AVAILABLE -> Color(0xFF10B981)
        SlotStatus.LIMITED -> Color(0xFFF59E0B)
        SlotStatus.FULL -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else surfaceColor
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = Brush.linearGradient(listOf(accentColor, accentColor))
        ) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Availability badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${slotInfo.availableSpots} Available",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                if (slotInfo.status == SlotStatus.FULL) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "FULL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle icon
            Icon(
                imageVector = slotInfo.icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle type
            Text(
                text = slotInfo.vehicleType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = slotInfo.description,
                fontSize = 11.sp,
                color = subtextColor
            )
        }
    }
}

@Composable
fun QuickInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = subtextColor
            )
        }
    }
}

@Composable
fun FacilityItem(
    icon: ImageVector,
    text: String,
    accentColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor
        )
    }
}