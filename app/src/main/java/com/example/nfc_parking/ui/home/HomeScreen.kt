package com.example.nfc_parking.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavHostController
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.navigation.NavRoutes
import com.example.nfc_parking.data.SavedLocationsManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    navController: NavHostController,
    onLocationClick: (ParkingLocation) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isDarkTheme by ThemeManager.isDarkTheme

    // 🎨 Color Scheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color.Black else Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null, tint = if (selectedTab == 0) accentColor else Color.Gray) },
                    label = { Text("Home", color = if (selectedTab == 0) accentColor else Color.Gray) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            null,
                            tint = if (selectedTab == 1) accentColor else Color.Gray
                        )
                    },
                    label = { Text("Saved", color = if (selectedTab == 1) accentColor else Color.Gray) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, null, tint = if (selectedTab == 2) accentColor else Color.Gray) },
                    label = { Text("History", color = if (selectedTab == 2) accentColor else Color.Gray) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        // Navigate to full ProfileScreen
                        navController.navigate(NavRoutes.PROFILE)
                    },
                    icon = { Icon(Icons.Default.Person, null, tint = if (selectedTab == 3) accentColor else Color.Gray) },
                    label = { Text("Profile", color = if (selectedTab == 3) accentColor else Color.Gray) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(onLocationClick = onLocationClick)
                1 -> SavedTabContent()
                2 -> HistoryTabContent()
                // Profile navigates to separate screen, no content needed here
            }
        }
    }
}

@Composable
fun HomeTabContent(
    onLocationClick: (ParkingLocation) -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    var searchQuery by remember { mutableStateOf("") }

    // 🎨 Color Scheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White

    // 📍 Sample Parking Locations
    val allParkingLocations = remember {
        listOf(
            ParkingLocation(
                id = "1",
                name = "Illinois Center",
                address = "111 E Wacker Dr, Chicago",
                distance = "1.4 km",
                duration = "5 min",
                pricePerHour = "$5",
                availableSpots = 24,
                accentColor = Color(0xFF9ACC06)
            ),
            ParkingLocation(
                id = "2",
                name = "AON Center Parking",
                address = "200 E Randolph St, Chicago",
                distance = "2.1 km",
                duration = "8 min",
                pricePerHour = "$6",
                availableSpots = 15,
                accentColor = Color(0xFF00D9FF)
            ),
            ParkingLocation(
                id = "3",
                name = "Millennium Park Garage",
                address = "5 S Columbus Dr, Chicago",
                distance = "0.8 km",
                duration = "3 min",
                pricePerHour = "$8",
                availableSpots = 42,
                accentColor = Color(0xFFFF6B9D)
            ),
            ParkingLocation(
                id = "4",
                name = "Navy Pier Parking",
                address = "600 E Grand Ave, Chicago",
                distance = "3.2 km",
                duration = "12 min",
                pricePerHour = "$10",
                availableSpots = 8,
                accentColor = Color(0xFFFFB800)
            ),
            ParkingLocation(
                id = "5",
                name = "Mag Mile Plaza",
                address = "Water Tower Place, Chicago",
                distance = "1.9 km",
                duration = "7 min",
                pricePerHour = "$7",
                availableSpots = 31,
                accentColor = Color(0xFF9D4EDD)
            ),
            ParkingLocation(
                id = "6",
                name = "Grant Park Underground",
                address = "337 E Randolph St, Chicago",
                distance = "1.1 km",
                duration = "4 min",
                pricePerHour = "$6",
                availableSpots = 19,
                accentColor = Color(0xFF05B273)
            )
        )
    }

    // 🔍 Filtered parking locations based on search
    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allParkingLocations
        } else {
            allParkingLocations.filter { location ->
                location.name.contains(searchQuery, ignoreCase = true) ||
                        location.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 🔝 Top Bar with Theme Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = accentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Santa Ana, Illinois 85486",
                    color = textColor,
                    fontSize = 14.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🌗 THEME TOGGLE
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { ThemeManager.toggleTheme(!isDarkTheme) },
                    color = cardColor,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 🔔 NOTIFICATION ICON
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🧠 Heading
        Text(
            text = "Find Your\nParking Space",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔍 Search Bar
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search parking locations...", color = subtextColor)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = subtextColor
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = subtextColor
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = surfaceColor,
                    focusedContainerColor = surfaceColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = accentColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor, CircleShape)
                    .clickable {
                        // Optional: You can trigger search or filter action here
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (isDarkTheme) Color.Black else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Show message if no results
        if (filteredLocations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No parking locations found",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try a different search term",
                        color = subtextColor,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // 🅿️ PARKING CARDS LIST (VERTICAL SCROLLING)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredLocations.size) { index ->
                    val location = filteredLocations[index]
                    ParkingLocationCard(
                        location = location,
                        isDarkTheme = isDarkTheme,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        onClick = { onLocationClick(location) }
                    )
                }
            }
        }
    }
}

@Composable
fun ParkingLocationCard(
    location: ParkingLocation,
    isDarkTheme: Boolean,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit = {}
) {
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val isSaved = SavedLocationsManager.isSaved(location)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🖼️ Image Section with Accent Color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                location.accentColor.copy(alpha = 0.3f),
                                location.accentColor.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Parking Icon
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = location.accentColor,
                    modifier = Modifier.size(80.dp)
                )

                // Available Spots Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = location.accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${location.availableSpots} spots",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // 🔖 BOOKMARK BUTTON
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(40.dp)
                        .clickable { SavedLocationsManager.toggleSave(location) },
                    shape = CircleShape,
                    color = if (isDarkTheme) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save location",
                            tint = if (isSaved) location.accentColor else (if (isDarkTheme) Color.White else Color.Black),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 📝 Info Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = location.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = location.address,
                        fontSize = 14.sp,
                        color = subtextColor,
                        maxLines = 1
                    )
                }

                // Distance, Duration, and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = Icons.Default.Place,
                        text = location.distance,
                        accentColor = location.accentColor,
                        isDarkTheme = isDarkTheme
                    )

                    InfoChip(
                        icon = Icons.Default.Schedule,
                        text = location.duration,
                        accentColor = location.accentColor,
                        isDarkTheme = isDarkTheme
                    )

                    InfoChip(
                        icon = Icons.Default.AttachMoney,
                        text = location.pricePerHour,
                        accentColor = location.accentColor,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    accentColor: Color,
    isDarkTheme: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SavedTabContent() {
    val isDarkTheme by ThemeManager.isDarkTheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    val savedLocations = SavedLocationsManager.savedLocations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Locations",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (savedLocations.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${savedLocations.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (savedLocations.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = subtextColor,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Saved Locations",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bookmark your favorite parking spots\nfrom the home screen",
                        color = subtextColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Saved Locations List (Stacked format with vertical scrolling)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedLocations.size) { index ->
                    val location = savedLocations[index]
                    SavedLocationCard(
                        location = location,
                        isDarkTheme = isDarkTheme,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        cardColor = cardColor,
                        onRemove = { SavedLocationsManager.toggleSave(location) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedLocationCard(
    location: ParkingLocation,
    isDarkTheme: Boolean,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left colored section with parking icon
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                location.accentColor.copy(alpha = 0.3f),
                                location.accentColor.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = location.accentColor,
                    modifier = Modifier.size(50.dp)
                )
            }

            // Right info section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section with name and bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = location.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = location.address,
                            fontSize = 11.sp,
                            color = subtextColor,
                            maxLines = 1
                        )
                    }

                    // Remove bookmark button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove",
                            tint = location.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom section with info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallInfoChip(
                        icon = Icons.Default.Place,
                        text = location.distance,
                        accentColor = location.accentColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SmallInfoChip(
                        icon = Icons.Default.Schedule,
                        text = location.duration,
                        accentColor = location.accentColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SmallInfoChip(
                        icon = Icons.Default.AttachMoney,
                        text = location.pricePerHour,
                        accentColor = location.accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun SmallInfoChip(
    icon: ImageVector,
    text: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = text,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HistoryTabContent() {
    val isDarkTheme by ThemeManager.isDarkTheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)

    // Sample parking history data
    val parkingHistory = remember {
        listOf(
            ParkingHistoryItem(
                locationName = "Illinois Center",
                date = "Jan 28, 2026",
                duration = "2h 30m",
                cost = "$12.50",
                address = "111 E Wacker Dr, Chicago",
                accentColor = Color(0xFF9ACC06)
            ),
            ParkingHistoryItem(
                locationName = "Millennium Park Garage",
                date = "Jan 25, 2026",
                duration = "1h 15m",
                cost = "$10.00",
                address = "5 S Columbus Dr, Chicago",
                accentColor = Color(0xFFFF6B9D)
            ),
            ParkingHistoryItem(
                locationName = "Navy Pier Parking",
                date = "Jan 22, 2026",
                duration = "3h 45m",
                cost = "$37.50",
                address = "600 E Grand Ave, Chicago",
                accentColor = Color(0xFFFFB800)
            ),
            ParkingHistoryItem(
                locationName = "AON Center Parking",
                date = "Jan 20, 2026",
                duration = "4h 00m",
                cost = "$24.00",
                address = "200 E Randolph St, Chicago",
                accentColor = Color(0xFF00D9FF)
            ),
            ParkingHistoryItem(
                locationName = "Grant Park Underground",
                date = "Jan 18, 2026",
                duration = "1h 30m",
                cost = "$9.00",
                address = "337 E Randolph St, Chicago",
                accentColor = Color(0xFF05B273)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Parking History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${parkingHistory.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HISTORY ITEMS LIST (VERTICAL SCROLLING)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(parkingHistory.size) { index ->
                val historyItem = parkingHistory[index]
                HistoryCard(
                    historyItem = historyItem,
                    isDarkTheme = isDarkTheme,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    cardColor = cardColor
                )
            }
        }
    }
}

// Data class for parking history
data class ParkingHistoryItem(
    val locationName: String,
    val date: String,
    val duration: String,
    val cost: String,
    val address: String,
    val accentColor: Color
)

@Composable
fun HistoryCard(
    historyItem: ParkingHistoryItem,
    isDarkTheme: Boolean,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left colored section
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                historyItem.accentColor.copy(alpha = 0.3f),
                                historyItem.accentColor.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = historyItem.accentColor,
                    modifier = Modifier.size(50.dp)
                )

                // Date badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = historyItem.accentColor
                ) {
                    Text(
                        text = historyItem.date,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Right info section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = historyItem.locationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = historyItem.address,
                        fontSize = 11.sp,
                        color = subtextColor,
                        maxLines = 1
                    )
                }

                // Stats section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallHistoryInfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = historyItem.duration,
                        accentColor = historyItem.accentColor,
                        subtextColor = subtextColor
                    )

                    SmallHistoryInfoRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Cost",
                        value = historyItem.cost,
                        accentColor = historyItem.accentColor,
                        subtextColor = subtextColor
                    )
                }
            }
        }
    }
}

@Composable
fun SmallHistoryInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    subtextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = subtextColor
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
fun ProfileTabContent(
    onLogout: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Profile",
            color = textColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLogout() },
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Logout", color = if (isDarkTheme) Color.Black else Color.White)
        }
    }
}