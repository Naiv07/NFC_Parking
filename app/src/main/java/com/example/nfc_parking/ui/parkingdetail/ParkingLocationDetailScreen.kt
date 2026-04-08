package com.example.nfc_parking.ui.parkingdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.ui.home.ParkingLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLocationDetailScreen(
    location: ParkingLocation,
    onBack: () -> Unit,
    onBookNow: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // ✅ ADD SELECTION STATE
    var selectedParkingType by remember { mutableStateOf("Compact Cars") }

    // Theme colors
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = location.name,  // ✅ PLAIN NAME - NO PLUS SIGNS
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Large Icon at top
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(location.accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold,
                        color = location.accentColor.copy(alpha = 0.3f)
                    )
                }
            }

            // Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Location Name and Address
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = location.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = location.address.replace("+", " "),
                                fontSize = 14.sp,
                                color = subtextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4.5",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < 4) Icons.Default.Star else Icons.Default.StarHalf,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(126 reviews)",
                                fontSize = 14.sp,
                                color = subtextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Price
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = location.pricePerHour,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = " /hour",
                                fontSize = 16.sp,
                                color = subtextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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

            // Available Spots Section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Available Spots",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        text = "${location.availableSpots} spots available",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Parking Type Cards - 4 options in 2 rows
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // First Row - Compact Cars & Sedan/SUV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ParkingTypeCard(
                            icon = "🚗",
                            title = "Compact Cars",
                            subtitle = "Easy to park",
                            available = "30 Available",
                            isSelected = selectedParkingType == "Compact Cars",
                            onClick = { selectedParkingType = "Compact Cars" },
                            containerColor = cardColor,
                            selectedColor = Color(0xFFFFC107),
                            textColor = textColor,
                            subtextColor = subtextColor,
                            modifier = Modifier.weight(1f)
                        )

                        ParkingTypeCard(
                            icon = "🚙",
                            title = "Sedan / SUV",
                            subtitle = "Mid-size zone",
                            available = "10 Available",
                            isSelected = selectedParkingType == "Sedan / SUV",
                            onClick = { selectedParkingType = "Sedan / SUV" },
                            containerColor = cardColor,
                            selectedColor = Color(0xFFFFC107),
                            textColor = textColor,
                            subtextColor = subtextColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Second Row - Large & Bike
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ParkingTypeCard(
                            icon = "🚐",
                            title = "Large",
                            subtitle = "Spacious",
                            available = "2 Available",
                            isSelected = selectedParkingType == "Large",
                            onClick = { selectedParkingType = "Large" },
                            containerColor = cardColor,
                            selectedColor = Color(0xFFFFC107),
                            textColor = textColor,
                            subtextColor = subtextColor,
                            modifier = Modifier.weight(1f)
                        )

                        ParkingTypeCard(
                            icon = "🏍️",
                            title = "Bike / Scooter",
                            subtitle = "Two-wheeler",
                            available = "50 Available",
                            isSelected = selectedParkingType == "Bike / Scooter",
                            onClick = { selectedParkingType = "Bike / Scooter" },
                            containerColor = cardColor,
                            selectedColor = Color(0xFFFFC107),
                            textColor = textColor,
                            subtextColor = subtextColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Most busy info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Most busy on Friday evenings",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Fixed Book Now Button at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = backgroundColor,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Book a Spot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ParkingTypeCard(
    icon: String,
    title: String,
    subtitle: String,
    available: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    selectedColor: Color,
    textColor: Color,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },  // ✅ CLICKABLE
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else containerColor  // ✅ HIGHLIGHT WHEN SELECTED
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp  // ✅ RAISE WHEN SELECTED
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, selectedColor.copy(alpha = 0.5f))
        else null  // ✅ BORDER WHEN SELECTED
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = icon,
                fontSize = 40.sp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else textColor  // ✅ BLACK TEXT WHEN SELECTED
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else subtextColor
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
                Text(
                    text = available,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}