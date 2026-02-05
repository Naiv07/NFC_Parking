package com.example.nfc_parking.ui.home

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
import androidx.navigation.NavHostController
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.AlertManager
import com.example.nfc_parking.navigation.NavRoutes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.filled.Bookmark

object SavedLocationsManager {
    private val _savedLocationIds = mutableStateListOf<String>()

    fun isLocationSaved(locationId: String): Boolean {
        return _savedLocationIds.contains(locationId)
    }

    fun toggleSaveLocation(locationId: String) {
        if (_savedLocationIds.contains(locationId)) {
            _savedLocationIds.remove(locationId)
        } else {
            _savedLocationIds.add(locationId)
        }
    }

    fun getSavedLocationIds(): List<String> {
        return _savedLocationIds.toList()
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    navController: NavHostController,
    onLocationClick: (ParkingLocation) -> Unit = {},
    onNavigateToBookings: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val unreadCount by remember { derivedStateOf { AlertManager.getUnreadCount() } }
    var selectedTab by remember { mutableStateOf(0) }

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
                    onClick = {
                        selectedTab = 0  // Reset to Home tab
                        onNavigateToBookings()  // Navigate to full BookingsHistoryScreen
                    },
                    icon = {
                        Icon(
                            Icons.Default.Receipt,
                            null,
                            tint = if (selectedTab == 2) accentColor else Color.Gray
                        )
                    },
                    label = { Text("Bookings", color = if (selectedTab == 2) accentColor else Color.Gray) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 0  // Reset to Home tab
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
                0 -> HomeTabContent(
                    navController = navController,
                    onLocationClick = onLocationClick
                )
                1 -> SavedTabContent()
                // selectedTab 2 (Bookings) navigates to separate screen, no content here
                // selectedTab 3 (Profile) navigates to separate screen, no content here
            }
        }
    }
}

@Composable
fun HomeTabContent(
    navController: NavHostController,
    onLocationClick: (ParkingLocation) -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val unreadCount by remember { derivedStateOf { AlertManager.getUnreadCount() } }
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
                name = "Connaught Place Parking",
                address = "CP Block, Connaught Place, New Delhi",
                distance = "1.4 km",
                duration = "5 min",
                pricePerHour = "Rs.5",
                availableSpots = 24,
                accentColor = Color(0xFF9ACC06)
            ),
            ParkingLocation(
                id = "2",
                name = "Cyber City Tower Parking",
                address = "DLF Cyber City, Gurgaon",
                distance = "2.1 km",
                duration = "8 min",
                pricePerHour = "Rs.6",
                availableSpots = 15,
                accentColor = Color(0xFF00D9FF)
            ),
            ParkingLocation(
                id = "3",
                name = "Select Citywalk Mall",
                address = "Saket, New Delhi",
                distance = "0.8 km",
                duration = "3 min",
                pricePerHour = "Rs.8",
                availableSpots = 42,
                accentColor = Color(0xFFFF6B9D)
            ),
            ParkingLocation(
                id = "4",
                name = "India Gate Parking",
                address = "Rajpath, New Delhi",
                distance = "3.2 km",
                duration = "12 min",
                pricePerHour = "Rs.10",
                availableSpots = 8,
                accentColor = Color(0xFFFFB800)
            ),
            ParkingLocation(
                id = "5",
                name = "Phoenix Marketcity",
                address = "LBS Marg, Kurla, Mumbai",
                distance = "1.9 km",
                duration = "7 min",
                pricePerHour = "Rs.7",
                availableSpots = 31,
                accentColor = Color(0xFF9D4EDD)
            ),
            ParkingLocation(
                id = "6",
                name = "Bangalore Central Mall",
                address = "MG Road, Bengaluru",
                distance = "1.1 km",
                duration = "4 min",
                pricePerHour = "Rs.6",
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

                // 🔔 NOTIFICATION ICON WITH BADGE
                Box {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate(NavRoutes.ALERTS)
                            },
                        color = cardColor,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Unread Badge
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
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
fun SavedTabContent() {
    val isDarkTheme by ThemeManager.isDarkTheme
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val savedLocationIds = remember { SavedLocationsManager.getSavedLocationIds() }

    // Get all parking locations
    val allParkingLocations = remember {
        listOf(
            ParkingLocation(
                id = "1",
                name = "Illinois Center",
                address = "111 E Wacker Dr, Chicago",
                distance = "1.4 km",
                duration = "5 min",
                pricePerHour = "Rs.5",
                availableSpots = 24,
                accentColor = Color(0xFF9ACC06)
            ),
            ParkingLocation(
                id = "2",
                name = "AON Center Parking",
                address = "200 E Randolph St, Chicago",
                distance = "2.1 km",
                duration = "8 min",
                pricePerHour = "Rs.6",
                availableSpots = 15,
                accentColor = Color(0xFF00D9FF)
            ),
            ParkingLocation(
                id = "3",
                name = "Millennium Park Garage",
                address = "5 S Columbus Dr, Chicago",
                distance = "0.8 km",
                duration = "3 min",
                pricePerHour = "Rs.8",
                availableSpots = 42,
                accentColor = Color(0xFFFF6B9D)
            ),
            ParkingLocation(
                id = "4",
                name = "Navy Pier Parking",
                address = "600 E Grand Ave, Chicago",
                distance = "3.2 km",
                duration = "12 min",
                pricePerHour = "Rs.10",
                availableSpots = 8,
                accentColor = Color(0xFFFFB800)
            ),
            ParkingLocation(
                id = "5",
                name = "Mag Mile Plaza",
                address = "Water Tower Place, Chicago",
                distance = "1.9 km",
                duration = "7 min",
                pricePerHour = "Rs.7",
                availableSpots = 31,
                accentColor = Color(0xFF9D4EDD)
            ),
            ParkingLocation(
                id = "6",
                name = "Grant Park Underground",
                address = "337 E Randolph St, Chicago",
                distance = "1.1 km",
                duration = "4 min",
                pricePerHour = "Rs.6",
                availableSpots = 19,
                accentColor = Color(0xFF05B273)
            )
        )
    }

    // Filter to only saved locations
    val savedLocations = allParkingLocations.filter {
        savedLocationIds.contains(it.id)
    }

    if (savedLocations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = subtextColor,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Saved Locations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Save your favorite parking spots here",
                    fontSize = 14.sp,
                    color = subtextColor
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(savedLocations.size) { index ->
                val location = savedLocations[index]
                ParkingLocationCard(
                    location = location,
                    isDarkTheme = isDarkTheme,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    onClick = { /* Navigate to details */ }
                )
            }
        }
    }
}

// 🅿️ PARKING LOCATION CARD
@Composable
fun ParkingLocationCard(
    location: ParkingLocation,
    isDarkTheme: Boolean,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    var isSaved by remember { mutableStateOf(SavedLocationsManager.isLocationSaved(location.id)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Left: Image Placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(location.accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = location.accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: Details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Name, Address, and Bookmark
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = location.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = location.address,
                                fontSize = 12.sp,
                                color = subtextColor,
                                maxLines = 1
                            )
                        }

                        // Bookmark Icon
                        IconButton(
                            onClick = {
                                SavedLocationsManager.toggleSaveLocation(location.id)
                                isSaved = !isSaved
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Remove from saved" else "Save location",
                                tint = if (isSaved) location.accentColor else subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom section: Distance, Time, Price, and Available Spots
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = subtextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location.distance,
                                fontSize = 12.sp,
                                color = subtextColor
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = subtextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location.duration,
                                fontSize = 12.sp,
                                color = subtextColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${location.pricePerHour}/hr",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = location.accentColor
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (location.availableSpots > 10)
                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else
                                Color(0xFFFF9800).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${location.availableSpots} spots",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (location.availableSpots > 10)
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// NOTE: ParkingLocation data class should be defined in a separate file
// or imported from wherever it's already defined in your project