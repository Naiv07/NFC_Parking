package com.example.nfc_parking.ui.selectspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.Booking
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.data.ThemeManager
import kotlinx.coroutines.launch
import kotlin.random.Random

// Parking space data model
data class ParkingSpace(
    val id: String,
    val label: String,
    val isAvailable: Boolean,
    val isEVCharging: Boolean = false,
    val section: String
)

// Floor data model
data class ParkingFloor(
    val floorNumber: Int,
    val floorName: String,
    val sections: List<ParkingSection>
)

// Section data model
data class ParkingSection(
    val sectionName: String,
    val spaces: List<ParkingSpace>,
    val availableCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectParkingSpaceScreen(
    locationId: String,
    locationName: String,
    onBack: () -> Unit,
    onNavigateToPayment: (Booking) -> Unit = {}
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var selectedFloor by remember { mutableStateOf(1) }
    var selectedSpace by remember { mutableStateOf<ParkingSpace?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf(2) }
    var isBooking by remember { mutableStateOf(false) }
    var bookingError by remember { mutableStateOf<String?>(null) }

    // Get booked spaces from BookingManager (real-time)
    val bookedSpaces by BookingManager.bookedSpaces

    // 🎨 Color Scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF7CB342)
    val selectedFloorColor = if (isDarkTheme) Color.Black else Color.Black

    // Start listening for bookings when screen opens
    LaunchedEffect(locationId) {
        BookingManager.startListening(locationId)
    }

    // Stop listening when screen closes
    DisposableEffect(Unit) {
        onDispose {
            BookingManager.stopListening()
        }
    }

    // Generate floors for this location
    val floors = remember(locationId) {
        generateFloors(locationId)
    }

    val currentFloor = floors.find { it.floorNumber == selectedFloor } ?: floors[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔝 TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(cardColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = locationName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Select Parking Space",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                }

                IconButton(
                    onClick = { /* TODO: Open QR scanner */ },
                    modifier = Modifier.background(cardColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🏢 FLOOR SELECTOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                floors.forEach { floor ->
                    FloorChip(
                        floorName = floor.floorName,
                        isSelected = selectedFloor == floor.floorNumber,
                        selectedColor = selectedFloorColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = { selectedFloor = floor.floorNumber }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 📍 FLOOR PLAN SECTIONS
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(currentFloor.sections.size) { sectionIndex ->
                    val section = currentFloor.sections[sectionIndex]
                    ParkingSectionView(
                        section = section,
                        selectedSpace = selectedSpace,
                        bookedSpaces = bookedSpaces,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                        onSpaceClick = { space ->
                            // Check if space is booked
                            if (BookingManager.isSpaceBooked(space.id)) {
                                return@ParkingSectionView
                            }

                            // Toggle selection
                            selectedSpace = if (selectedSpace?.id == space.id) {
                                null
                            } else {
                                space
                            }
                        }
                    )
                }
            }
        }

        // 💳 BOTTOM BOOKING BAR
        if (selectedSpace != null && !showConfirmation) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = selectedFloorColor,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clickable { showConfirmation = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selected: ${selectedSpace!!.label}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap to continue",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Show error if booking fails
        if (bookingError != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { bookingError = null }) {
                        Text("OK")
                    }
                }
            ) {
                Text(bookingError!!)
            }
        }
    }

    // 🎯 CONFIRMATION BOTTOM SHEET
    if (showConfirmation && selectedSpace != null) {
        ModalBottomSheet(
            onDismissRequest = { showConfirmation = false },
            sheetState = sheetState,
            containerColor = cardColor,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ConfirmBookingSheet(
                locationName = locationName,
                parkingSpace = selectedSpace!!,
                accentColor = accentColor,
                textColor = textColor,
                subtextColor = subtextColor,
                cardColor = cardColor,
                isDarkTheme = isDarkTheme,
                initialDuration = selectedDuration,
                isBooking = isBooking,
                onDurationChange = { duration -> selectedDuration = duration },
                onConfirm = {
                    scope.launch {
                        isBooking = true

                        // Get current user info
                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val userName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

                        // Book the space
                        val result = BookingManager.bookSpace(
                            spaceId = selectedSpace!!.id,
                            userId = userId,
                            userName = userName,
                            locationId = locationId,
                            locationName = locationName,
                            spaceLabel = selectedSpace!!.label,
                            durationHours = selectedDuration
                        )

                        isBooking = false

                        result.onSuccess { booking ->
                            sheetState.hide()
                            showConfirmation = false
                            selectedSpace = null
                            onNavigateToPayment(booking)
                        }.onFailure { error ->
                            bookingError = error.message
                            sheetState.hide()
                            showConfirmation = false
                        }
                    }
                },
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showConfirmation = false
                    }
                },
                onPaymentMethodClick = {
                    scope.launch {
                        isBooking = true

                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val userName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

                        // ✅ Use reserveSpace instead of bookSpace
                        val result = BookingManager.reserveSpace(
                            spaceId = selectedSpace!!.id,
                            userId = userId,
                            userName = userName,
                            locationId = locationId,
                            locationName = locationName,
                            spaceLabel = selectedSpace!!.label,
                            durationHours = selectedDuration
                        )

                        isBooking = false

                        result.onSuccess { booking ->
                            sheetState.hide()
                            showConfirmation = false
                            selectedSpace = null
                            onNavigateToPayment(booking)
                        }.onFailure { error ->
                            bookingError = error.message
                            sheetState.hide()
                            showConfirmation = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FloorChip(
    floorName: String,
    isSelected: Boolean,
    selectedColor: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) selectedColor else cardColor
    ) {
        Text(
            text = floorName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else textColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun ConfirmBookingSheet(
    locationName: String,
    parkingSpace: ParkingSpace,
    accentColor: Color,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color,
    isDarkTheme: Boolean,
    initialDuration: Int = 2,
    isBooking: Boolean = false,
    onDurationChange: (Int) -> Unit = {},
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onPaymentMethodClick: () -> Unit
) {
    var selectedHours by remember { mutableStateOf(initialDuration) }
    val maxHours = 12

    // Update parent when duration changes
    LaunchedEffect(selectedHours) {
        onDurationChange(selectedHours)
    }

    // Live time tracking
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            currentTime = System.currentTimeMillis()
        }
    }

    // Helper function to format time
    fun formatTime(timeMillis: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    // Calculate end time
    val endTime = currentTime + (selectedHours * 60 * 60 * 1000L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Confirm Booking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .background(cardColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "$locationName in city",
            fontSize = 14.sp,
            color = subtextColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Parking Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Parking Zone",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Zone ${parkingSpace.section}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Parking Place",
                        fontSize = 12.sp,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = parkingSpace.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🕐 TIME SLOT SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF0F0F0F) else Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Duration",
                            fontSize = 14.sp,
                            color = subtextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$selectedHours ${if (selectedHours == 1) "hour" else "hours"}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    // Increment/Decrement Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decrease button
                        IconButton(
                            onClick = { if (selectedHours > 1) selectedHours-- },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (selectedHours > 1) accentColor else subtextColor.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            enabled = selectedHours > 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (selectedHours > 1) Color.Black else subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Increase button
                        IconButton(
                            onClick = { if (selectedHours < maxHours) selectedHours++ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (selectedHours < maxHours) accentColor else subtextColor.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            enabled = selectedHours < maxHours
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (selectedHours < maxHours) Color.Black else subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Slider
                Column {
                    Slider(
                        value = selectedHours.toFloat(),
                        onValueChange = { selectedHours = it.toInt() },
                        valueRange = 1f..maxHours.toFloat(),
                        steps = maxHours - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = subtextColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Time markers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1h",
                            fontSize = 12.sp,
                            color = subtextColor
                        )
                        Text(
                            text = "6h",
                            fontSize = 12.sp,
                            color = subtextColor
                        )
                        Text(
                            text = "12h",
                            fontSize = 12.sp,
                            color = subtextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time breakdown with LIVE TIME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start Time",
                                fontSize = 12.sp,
                                color = subtextColor
                            )
                        }
                        Text(
                            text = formatTime(currentTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "End Time",
                                fontSize = 12.sp,
                                color = subtextColor
                            )
                        }
                        Text(
                            text = formatTime(endTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price and Confirm Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$${String.format("%.2f", 4.12 * selectedHours)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "$4.12/hr × $selectedHours hr",
                    fontSize = 12.sp,
                    color = subtextColor
                )
            }

            Button(
                onClick = onPaymentMethodClick,
                enabled = !isBooking,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Confirm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ParkingSectionView(
    section: ParkingSection,
    selectedSpace: ParkingSpace?,
    bookedSpaces: Set<String>,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    onSpaceClick: (ParkingSpace) -> Unit
) {
    // Filter to only show available spaces (remove booked ones)
    val availableSpaces = section.spaces.filter { it.id !in bookedSpaces }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${section.sectionName} 01-06",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = "${section.spaces.count { !BookingManager.isSpaceBooked(it.id) }} Space Available",
                fontSize = 13.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entrance/Exit labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Exit", fontSize = 11.sp, color = subtextColor)
            Text(text = "Entry", fontSize = 11.sp, color = subtextColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Parking spaces grid
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row (first 3 spaces)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..2) {
                        if (i < section.spaces.size) {
                            ParkingSpaceBox(
                                space = section.spaces[i],
                                isSelected = selectedSpace?.id == section.spaces[i].id,
                                isBooked = BookingManager.isSpaceBooked(section.spaces[i].id),
                                accentColor = accentColor,
                                onClick = { onSpaceClick(section.spaces[i]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.size(90.dp, 80.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main entrance indicator
                if (section.sectionName == "A") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Main Entrance",
                            fontSize = 10.sp,
                            color = subtextColor,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row (next 3 spaces)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 3..5) {
                        if (i < section.spaces.size) {
                            ParkingSpaceBox(
                                space = section.spaces[i],
                                isSelected = selectedSpace?.id == section.spaces[i].id,
                                isBooked = BookingManager.isSpaceBooked(section.spaces[i].id),
                                accentColor = accentColor,
                                onClick = { onSpaceClick(section.spaces[i]) }
                            )
                        } else {
                            Spacer(modifier = Modifier.size(90.dp, 80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParkingSpaceBox(
    space: ParkingSpace,
    isSelected: Boolean,
    isBooked: Boolean = false,
    accentColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isBooked -> Color.Black
        isSelected -> accentColor
        else -> accentColor.copy(alpha = 0.3f)
    }

    val borderColor = if (isSelected && !isBooked) accentColor else Color.Transparent

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected && !isBooked) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isBooked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (space.isEVCharging && !isBooked) {
                Icon(
                    imageVector = Icons.Default.EvStation,
                    contentDescription = "EV Charging",
                    tint = if (isBooked) Color.White else Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = space.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBooked) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )

            if (isBooked) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Booked",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Generate parking floors - ALL SPACES AVAILABLE
fun generateFloors(locationId: String): List<ParkingFloor> {
    val random = Random(locationId.hashCode())

    return listOf(
        generateFloor(1, "1st Floor", random),
        generateFloor(2, "2nd Floor", random),
        generateFloor(3, "3rd Floor", random)
    )
}

fun generateFloor(floorNumber: Int, floorName: String, random: Random): ParkingFloor {
    val sections = listOf("A", "B", "C").map { sectionName ->
        val spaces = (1..6).map { spaceNum ->
            val isEVCharging = random.nextFloat() > 0.8f

            ParkingSpace(
                id = "$floorNumber-$sectionName-${String.format("%02d", spaceNum)}",
                label = "$sectionName-${String.format("%02d", spaceNum)}",
                isAvailable = true,
                isEVCharging = isEVCharging,
                section = sectionName
            )
        }

        ParkingSection(
            sectionName = sectionName,
            spaces = spaces,
            availableCount = spaces.size
        )
    }

    return ParkingFloor(
        floorNumber = floorNumber,
        floorName = floorName,
        sections = sections
    )
}