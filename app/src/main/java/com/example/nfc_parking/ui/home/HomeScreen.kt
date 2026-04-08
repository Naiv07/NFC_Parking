package com.example.nfc_parking.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.AlertsManager
import com.example.nfc_parking.data.LocationManager
import com.example.nfc_parking.data.UserLocation
import com.example.nfc_parking.navigation.NavRoutes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.filled.Bookmark
import com.example.nfc_parking.data.BookingManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


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

// Filter options enum
enum class ParkingFilter {
    NEAREST,
    FREE_SLOTS,
    RATING
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    navController: NavHostController,
    onLocationClick: (ParkingLocation) -> Unit = {},
    onNavigateToBookings: () -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val unreadCount by remember { derivedStateOf { AlertsManager.getUnreadCount() } }
    var selectedTab by remember { mutableStateOf(0) }

    // ✅ INTERCEPT BACK BUTTON - Always return to Home tab
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0  // Go back to Home tab instead of exiting
    }

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
                        android.util.Log.d("HomeScreen", "Bookings tab clicked!")
                        onNavigateToBookings()
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
                        navController.navigate(NavRoutes.PROFILE)  // Just navigate
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
            }
        }
    }
}

@Composable
fun HomeTabContent(
    navController: NavHostController,
    onLocationClick: (ParkingLocation) -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkTheme by ThemeManager.isDarkTheme
    val unreadCount by remember { derivedStateOf { AlertsManager.getUnreadCount() } }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ParkingFilter.NEAREST) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // 📍 REAL-TIME LOCATION STATE
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoadingLocation by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // 🎨 Color Scheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White


    // 📍 GET REAL-TIME LOCATION
    LaunchedEffect(Unit) {
        scope.launch {
            // Show loading
            isLoadingLocation = true

            val result = LocationManager.getCurrentLocation(context)
            result.onSuccess { location ->
                userLocation = location
                isLoadingLocation = false
                locationError = null
            }.onFailure { error ->
                // DON'T show error - use default location instead
                userLocation = UserLocation(
                    latitude = 12.9716,  // Default to Bangalore
                    longitude = 77.5946,
                    accuracy = 1000f
                )
                isLoadingLocation = false
                locationError = null  // Don't show error
            }
        }
    }

    // 📍 25 INDIAN PARKING LOCATIONS with GPS coordinates
    val allParkingLocations = remember {
        listOf(
            ParkingLocation("1", "Indiranagar Metro Parking", "100 Feet Road, Indiranagar, Bangalore", "1.2 km", "4 min", "Rs.20", 35, 50, Color(0xFF9ACC06), 12.9716, 77.6412),
            ParkingLocation("2", "MG Road Plaza", "Brigade Road, MG Road, Bangalore", "0.8 km", "3 min", "Rs.30", 28, 40, Color(0xFF00D9FF), 12.9716, 77.6003),
            ParkingLocation("3", "Koramangala Forum Mall", "Hosur Main Road, Koramangala, Bangalore", "2.5 km", "9 min", "Rs.25", 42, 60, Color(0xFFFF6B9D), 12.9352, 77.6245),
            ParkingLocation("4", "Electronic City Tech Park", "Hosur Road, Electronic City, Bangalore", "18.2 km", "35 min", "Rs.15", 65, 100, Color(0xFFFFB800), 12.8456, 77.6603),
            ParkingLocation("5", "Whitefield IT Hub", "ITPL Main Road, Whitefield, Bangalore", "15.8 km", "32 min", "Rs.18", 50, 80, Color(0xFF9D4EDD), 12.9698, 77.7499),
            ParkingLocation("6", "Bandra Kurla Complex", "BKC, Bandra East, Mumbai", "8.5 km", "22 min", "Rs.40", 38, 60, Color(0xFF05B273), 19.0596, 72.8656),
            ParkingLocation("7", "Andheri Metro Station", "Western Express Highway, Andheri, Mumbai", "12.3 km", "28 min", "Rs.35", 45, 70, Color(0xFFE63946), 19.1197, 72.8464),
            ParkingLocation("8", "Lower Parel Mall", "High Street Phoenix, Lower Parel, Mumbai", "6.2 km", "18 min", "Rs.50", 25, 40, Color(0xFFF77F00), 19.0095, 72.8295),
            ParkingLocation("9", "Marine Drive Plaza", "Netaji Subhash Road, Marine Drive, Mumbai", "4.8 km", "15 min", "Rs.60", 18, 30, Color(0xFF06FFA5), 18.9432, 72.8236),
            ParkingLocation("10", "Powai IT Park", "Hiranandani Gardens, Powai, Mumbai", "10.5 km", "25 min", "Rs.28", 55, 80, Color(0xFF4361EE), 19.1197, 72.9059),
            ParkingLocation("11", "Connaught Place Center", "Inner Circle, CP, New Delhi", "3.2 km", "12 min", "Rs.35", 40, 60, Color(0xFFFF006E), 28.6315, 77.2167),
            ParkingLocation("12", "Nehru Place Metro", "Nehru Place, South Delhi", "8.8 km", "20 min", "Rs.25", 52, 80, Color(0xFF8338EC), 28.5494, 77.2501),
            ParkingLocation("13", "Cyber Hub Gurgaon", "DLF Cyber City, Gurgaon", "22.5 km", "40 min", "Rs.30", 70, 120, Color(0xFFFB5607), 28.4942, 77.0892),
            ParkingLocation("14", "Saket Select City", "District Center, Saket, Delhi", "12.2 km", "28 min", "Rs.40", 35, 50, Color(0xFF3A86FF), 28.5244, 77.2066),
            ParkingLocation("15", "Rajiv Chowk Metro", "Barakhamba Road, Rajiv Chowk, Delhi", "2.8 km", "10 min", "Rs.30", 30, 45, Color(0xFFFFBE0B), 28.6328, 77.2197),
            ParkingLocation("16", "HITEC City Tech Hub", "HITEC City, Madhapur, Hyderabad", "15.2 km", "30 min", "Rs.22", 60, 100, Color(0xFF06FFA5), 17.4435, 78.3772),
            ParkingLocation("17", "Banjara Hills Mall", "Road No 1, Banjara Hills, Hyderabad", "8.5 km", "20 min", "Rs.28", 38, 60, Color(0xFFFF006E), 17.4239, 78.4738),
            ParkingLocation("18", "Gachibowli IT Park", "ORR, Gachibowli, Hyderabad", "18.8 km", "35 min", "Rs.20", 72, 120, Color(0xFF4CC9F0), 17.4399, 78.3489),
            ParkingLocation("19", "Begumpet Airport", "Begumpet, Hyderabad", "5.2 km", "15 min", "Rs.35", 45, 70, Color(0xFF9D4EDD), 17.4515, 78.4673),
            ParkingLocation("20", "Kukatpally Hub", "KPHB Colony, Kukatpally, Hyderabad", "12.5 km", "28 min", "Rs.18", 55, 80, Color(0xFF06D6A0), 17.4948, 78.3914),
            ParkingLocation("21", "Hinjewadi IT Park", "Phase 1, Hinjewadi, Pune", "20.5 km", "38 min", "Rs.15", 80, 150, Color(0xFFFFC300), 18.5912, 73.7389),
            ParkingLocation("22", "Koregaon Park Plaza", "North Main Road, Koregaon Park, Pune", "4.8 km", "15 min", "Rs.30", 32, 50, Color(0xFF06FFA5), 18.5362, 73.8958),
            ParkingLocation("23", "Viman Nagar Airport", "Airport Road, Viman Nagar, Pune", "8.2 km", "20 min", "Rs.40", 40, 60, Color(0xFF4361EE), 18.5679, 73.9143),
            ParkingLocation("24", "Aundh IT Hub", "Aundh-Baner Road, Aundh, Pune", "10.5 km", "25 min", "Rs.22", 48, 70, Color(0xFFFF006E), 18.5642, 73.8077),
            ParkingLocation("25", "Deccan Gymkhana", "FC Road, Deccan, Pune", "6.2 km", "18 min", "Rs.25", 35, 50, Color(0xFF8338EC), 18.5089, 73.8429)
        )
    }

    // ✅ STATE for locations with updated spots
    var parkingLocationsWithUpdatedSpots by remember {
        mutableStateOf(allParkingLocations)
    }

    // ✅ UPDATE AVAILABLE SPOTS IN REAL-TIME
    LaunchedEffect(Unit) {
        while (true) {
            android.util.Log.d("HomeScreen", "🔄 Updating available spots...")

            // Update spots for all locations
            val updatedLocations = parkingLocationsWithUpdatedSpots.map { location ->
                scope.async {
                    val availableSpots = BookingManager.getAvailableSpots(
                        location.id,
                        location.totalSpots
                    )
                    location.copy(availableSpots = availableSpots)
                }
            }.awaitAll()

            parkingLocationsWithUpdatedSpots = updatedLocations
            android.util.Log.d("HomeScreen", "✅ Updated ${updatedLocations.size} locations")

            delay(15000) // Update every 15 seconds
        }
    }

    // 📍 CALCULATE REAL DISTANCES (use parkingLocationsWithUpdatedSpots)
    val parkingLocationsWithRealDistances = remember(userLocation, parkingLocationsWithUpdatedSpots) {
        if (userLocation != null) {
            parkingLocationsWithUpdatedSpots.map { location ->
                val distanceKm = LocationManager.calculateDistance(
                    userLocation!!.latitude,
                    userLocation!!.longitude,
                    location.latitude,
                    location.longitude
                )
                val distanceText = LocationManager.formatDistance(distanceKm)
                val durationText = LocationManager.estimateTravelTime(distanceKm)

                location.copy(
                    distance = distanceText,
                    duration = durationText
                )
            }
        } else {
            parkingLocationsWithUpdatedSpots
        }
    }

    // 🔍 APPLY FILTERS (use parkingLocationsWithRealDistances)
    val filteredAndSortedLocations = remember(searchQuery, parkingLocationsWithRealDistances, selectedFilter) {
        var result = parkingLocationsWithRealDistances

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            result = result.filter { location ->
                location.name.contains(searchQuery, ignoreCase = true) ||
                        location.address.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply sorting
        when (selectedFilter) {
            ParkingFilter.NEAREST -> {
                result.sortedBy {
                    val distStr = it.distance.replace(" km", "").replace(" m", "")
                    distStr.toDoubleOrNull() ?: Double.MAX_VALUE
                }
            }
            ParkingFilter.FREE_SLOTS -> {
                result.sortedByDescending { it.availableSpots }
            }
            ParkingFilter.RATING -> {
                result.sortedByDescending { it.availableSpots }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 🔝 Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📍 REAL-TIME LOCATION DISPLAY - CLICKABLE TO REFRESH
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Refresh location when tapped
                        isLoadingLocation = true
                        scope.launch {
                            val result = LocationManager.getCurrentLocation(context)
                            result.onSuccess { location ->
                                userLocation = location
                                isLoadingLocation = false
                                locationError = null
                            }.onFailure { error ->
                                // Use default location if error
                                userLocation = UserLocation(
                                    latitude = 12.9716,
                                    longitude = 77.5946,
                                    accuracy = 1000f
                                )
                                isLoadingLocation = false
                                locationError = null
                            }
                        }
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isLoadingLocation) subtextColor else accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isLoadingLocation) "Getting location..." else "Your Location",
                    color = if (isLoadingLocation) subtextColor else textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
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
                        .clickable {
                            ThemeManager.setTheme(!isDarkTheme)
                        },
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

        // 🔍 Search Bar + Filter Button
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

            // FILTER BUTTON
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(accentColor, CircleShape)
                        .clickable { showFilterMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (isDarkTheme) Color.Black else Color.White
                    )
                }

                // Filter Dropdown Menu
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    modifier = Modifier.background(cardColor)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NearMe,
                                    null,
                                    tint = if (selectedFilter == ParkingFilter.NEAREST) accentColor else subtextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Nearest First",
                                    color = if (selectedFilter == ParkingFilter.NEAREST) accentColor else textColor,
                                    fontWeight = if (selectedFilter == ParkingFilter.NEAREST) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            selectedFilter = ParkingFilter.NEAREST
                            showFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalParking,
                                    null,
                                    tint = if (selectedFilter == ParkingFilter.FREE_SLOTS) accentColor else subtextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Most Free Slots",
                                    color = if (selectedFilter == ParkingFilter.FREE_SLOTS) accentColor else textColor,
                                    fontWeight = if (selectedFilter == ParkingFilter.FREE_SLOTS) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            selectedFilter = ParkingFilter.FREE_SLOTS
                            showFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = if (selectedFilter == ParkingFilter.RATING) accentColor else subtextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Highest Rated",
                                    color = if (selectedFilter == ParkingFilter.RATING) accentColor else textColor,
                                    fontWeight = if (selectedFilter == ParkingFilter.RATING) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            selectedFilter = ParkingFilter.RATING
                            showFilterMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Show active filter
        if (selectedFilter != ParkingFilter.NEAREST) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when(selectedFilter) {
                        ParkingFilter.FREE_SLOTS -> "Sorted by: Most Free Slots"
                        ParkingFilter.RATING -> "Sorted by: Highest Rated"
                        else -> "Sorted by: Nearest First"
                    },
                    fontSize = 12.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // PARKING CARDS LIST
        if (filteredAndSortedLocations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
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
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAndSortedLocations.size) { index ->
                    ParkingLocationCard(
                        location = filteredAndSortedLocations[index],
                        isDarkTheme = isDarkTheme,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        onClick = { onLocationClick(filteredAndSortedLocations[index]) }
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

    val allParkingLocations = remember {
        listOf(
            ParkingLocation("1", "Indiranagar Metro Parking", "100 Feet Road, Indiranagar, Bangalore", "1.2 km", "4 min", "Rs.20", 35, 50, Color(0xFF9ACC06), 12.9716, 77.6412),
            ParkingLocation("2", "MG Road Plaza", "Brigade Road, MG Road, Bangalore", "0.8 km", "3 min", "Rs.30", 28, 40, Color(0xFF00D9FF), 12.9716, 77.6003),
            ParkingLocation("3", "Koramangala Forum Mall", "Hosur Main Road, Koramangala, Bangalore", "2.5 km", "9 min", "Rs.25", 42, 60, Color(0xFFFF6B9D), 12.9352, 77.6245),
            ParkingLocation("4", "Electronic City Tech Park", "Hosur Road, Electronic City, Bangalore", "18.2 km", "35 min", "Rs.15", 65, 100, Color(0xFFFFB800), 12.8456, 77.6603),
            ParkingLocation("5", "Whitefield IT Hub", "ITPL Main Road, Whitefield, Bangalore", "15.8 km", "32 min", "Rs.18", 50, 80, Color(0xFF9D4EDD), 12.9698, 77.7499),
            ParkingLocation("6", "Bandra Kurla Complex", "BKC, Bandra East, Mumbai", "8.5 km", "22 min", "Rs.40", 38, 60, Color(0xFF05B273), 19.0596, 72.8656),
            ParkingLocation("7", "Andheri Metro Station", "Western Express Highway, Andheri, Mumbai", "12.3 km", "28 min", "Rs.35", 45, 70, Color(0xFFE63946), 19.1197, 72.8464),
            ParkingLocation("8", "Lower Parel Mall", "High Street Phoenix, Lower Parel, Mumbai", "6.2 km", "18 min", "Rs.50", 25, 40, Color(0xFFF77F00), 19.0095, 72.8295),
            ParkingLocation("9", "Marine Drive Plaza", "Netaji Subhash Road, Marine Drive, Mumbai", "4.8 km", "15 min", "Rs.60", 18, 30, Color(0xFF06FFA5), 18.9432, 72.8236),
            ParkingLocation("10", "Powai IT Park", "Hiranandani Gardens, Powai, Mumbai", "10.5 km", "25 min", "Rs.28", 55, 80, Color(0xFF4361EE), 19.1197, 72.9059),
            ParkingLocation("11", "Connaught Place Center", "Inner Circle, CP, New Delhi", "3.2 km", "12 min", "Rs.35", 40, 60, Color(0xFFFF006E), 28.6315, 77.2167),
            ParkingLocation("12", "Nehru Place Metro", "Nehru Place, South Delhi", "8.8 km", "20 min", "Rs.25", 52, 80, Color(0xFF8338EC), 28.5494, 77.2501),
            ParkingLocation("13", "Cyber Hub Gurgaon", "DLF Cyber City, Gurgaon", "22.5 km", "40 min", "Rs.30", 70, 120, Color(0xFFFB5607), 28.4942, 77.0892),
            ParkingLocation("14", "Saket Select City", "District Center, Saket, Delhi", "12.2 km", "28 min", "Rs.40", 35, 50, Color(0xFF3A86FF), 28.5244, 77.2066),
            ParkingLocation("15", "Rajiv Chowk Metro", "Barakhamba Road, Rajiv Chowk, Delhi", "2.8 km", "10 min", "Rs.30", 30, 45, Color(0xFFFFBE0B), 28.6328, 77.2197),
            ParkingLocation("16", "HITEC City Tech Hub", "HITEC City, Madhapur, Hyderabad", "15.2 km", "30 min", "Rs.22", 60, 100, Color(0xFF06FFA5), 17.4435, 78.3772),
            ParkingLocation("17", "Banjara Hills Mall", "Road No 1, Banjara Hills, Hyderabad", "8.5 km", "20 min", "Rs.28", 38, 60, Color(0xFFFF006E), 17.4239, 78.4738),
            ParkingLocation("18", "Gachibowli IT Park", "ORR, Gachibowli, Hyderabad", "18.8 km", "35 min", "Rs.20", 72, 120, Color(0xFF4CC9F0), 17.4399, 78.3489),
            ParkingLocation("19", "Begumpet Airport", "Begumpet, Hyderabad", "5.2 km", "15 min", "Rs.35", 45, 70, Color(0xFF9D4EDD), 17.4515, 78.4673),
            ParkingLocation("20", "Kukatpally Hub", "KPHB Colony, Kukatpally, Hyderabad", "12.5 km", "28 min", "Rs.18", 55, 80, Color(0xFF06D6A0), 17.4948, 78.3914),
            ParkingLocation("21", "Hinjewadi IT Park", "Phase 1, Hinjewadi, Pune", "20.5 km", "38 min", "Rs.15", 80, 150, Color(0xFFFFC300), 18.5912, 73.7389),
            ParkingLocation("22", "Koregaon Park Plaza", "North Main Road, Koregaon Park, Pune", "4.8 km", "15 min", "Rs.30", 32, 50, Color(0xFF06FFA5), 18.5362, 73.8958),
            ParkingLocation("23", "Viman Nagar Airport", "Airport Road, Viman Nagar, Pune", "8.2 km", "20 min", "Rs.40", 40, 60, Color(0xFF4361EE), 18.5679, 73.9143),
            ParkingLocation("24", "Aundh IT Hub", "Aundh-Baner Road, Aundh, Pune", "10.5 km", "25 min", "Rs.22", 48, 70, Color(0xFFFF006E), 18.5642, 73.8077),
            ParkingLocation("25", "Deccan Gymkhana", "FC Road, Deccan, Pune", "6.2 km", "18 min", "Rs.25", 35, 50, Color(0xFF8338EC), 18.5089, 73.8429)
        )
    }

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
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Saved Locations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1F2937)
                )
                Text(
                    text = "Save your favorite parking spots here",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
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
                ParkingLocationCard(
                    location = savedLocations[index],
                    isDarkTheme = isDarkTheme,
                    textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937),
                    subtextColor = Color(0xFF9CA3AF),
                    onClick = { }
                )
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

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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

                        IconButton(
                            onClick = {
                                SavedLocationsManager.toggleSaveLocation(location.id)
                                isSaved = !isSaved
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isSaved) location.accentColor else subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

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