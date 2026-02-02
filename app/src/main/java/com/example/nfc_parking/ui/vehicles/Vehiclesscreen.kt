package com.example.nfc_parking.ui.vehicles

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import kotlinx.coroutines.launch

private const val TAG = "VehiclesScreen"

// Vehicle Type Template for display
data class VehicleTypeTemplate(
    val type: VehicleType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val buttonLabel: String  // ⭐ NEW: Dynamic button label
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehiclesScreen(
    onBack: () -> Unit
) {
    Log.d(TAG, "VehiclesScreen composing...")

    val isDarkTheme by ThemeManager.isDarkTheme
    var showAddDialog by remember { mutableStateOf(false) }

    // Define vehicle type templates for swipe display
    val vehicleTypeTemplates = remember {
        listOf(
            VehicleTypeTemplate(
                type = VehicleType.CAR,
                title = "Hassle-Free Parking",
                subtitle = "Cars",
                icon = Icons.Default.DirectionsCar,
                buttonLabel = "Add Car"  // ⭐ Dynamic label
            ),
            VehicleTypeTemplate(
                type = VehicleType.BIKE,
                title = "Quick Bike Parking",
                subtitle = "Two-Wheeler",
                icon = Icons.Default.TwoWheeler,
                buttonLabel = "Add Bike"  // ⭐ Dynamic label
            ),
            VehicleTypeTemplate(
                type = VehicleType.EV,
                title = "Electric Charging",
                subtitle = "Electric Vehicle",
                icon = Icons.Default.EvStation,
                buttonLabel = "Add EV"  // ⭐ Dynamic label
            ),
            VehicleTypeTemplate(
                type = VehicleType.TRUCK,
                title = "Commercial Parking",
                subtitle = "Heavy Vehicle",
                icon = Icons.Default.LocalShipping,
                buttonLabel = "Add Truck"  // ⭐ Dynamic label
            )
        )
    }

    // Pager state for vehicle types
    val pagerState = rememberPagerState(pageCount = { vehicleTypeTemplates.size })
    val currentTemplate = vehicleTypeTemplates.getOrNull(pagerState.currentPage)
        ?: vehicleTypeTemplates[0]
    val coroutineScope = rememberCoroutineScope()

    // 🎨 Color Scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF9FAFB)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val selectedButtonColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()  // ⭐ Avoid notch/status bar
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
                // Back button
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                Text(
                    text = "My Vehicles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                // Menu Button
                IconButton(onClick = { /* TODO: Open menu */ }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🚗 VEHICLE SECTION WITH HORIZONTAL PAGER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Title & Subtitle (updates based on swipe)
                Text(
                    text = currentTemplate.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentTemplate.subtitle,
                    fontSize = 15.sp,
                    color = subtextColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Vehicle Image Carousel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 40.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        val template = vehicleTypeTemplates.getOrNull(page)
                            ?: vehicleTypeTemplates[0]
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            // Vehicle Icon based on type
                            Icon(
                                imageVector = template.icon,
                                contentDescription = null,
                                tint = accentColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(160.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Page Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(vehicleTypeTemplates.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                                    height = 8.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pagerState.currentPage == index) accentColor
                                    else subtextColor.copy(alpha = 0.3f)
                                )
                        )
                        if (index < vehicleTypeTemplates.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔘 VEHICLE TYPE BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleTypeChip(
                    icon = Icons.Default.DirectionsCar,
                    label = "Car",
                    isSelected = currentTemplate.type == VehicleType.CAR,
                    selectedColor = selectedButtonColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )

                VehicleTypeChip(
                    icon = Icons.Default.TwoWheeler,
                    label = "Bike",
                    isSelected = currentTemplate.type == VehicleType.BIKE,
                    selectedColor = selectedButtonColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )

                VehicleTypeChip(
                    icon = Icons.Default.EvStation,
                    label = "EV",
                    isSelected = currentTemplate.type == VehicleType.EV,
                    selectedColor = selectedButtonColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )

                VehicleTypeChip(
                    icon = Icons.Default.LocalShipping,
                    label = "Truck",
                    isSelected = currentTemplate.type == VehicleType.TRUCK,
                    selectedColor = selectedButtonColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ➕ DYNAMIC ADD VEHICLE BUTTON
            Button(
                onClick = {
                    Log.d(TAG, "${currentTemplate.buttonLabel} button clicked")
                    showAddDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (isDarkTheme) Color.Black else Color.White
                )
            ) {
                Text(
                    text = currentTemplate.buttonLabel,  // ⭐ Dynamic button text
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // 📝 ADD VEHICLE DIALOG
        if (showAddDialog) {
            SimpleAddVehicleDialog(
                vehicleType = currentTemplate.buttonLabel,  // ⭐ Pass dynamic label
                onDismiss = { showAddDialog = false }
            )
        }
    }

    Log.d(TAG, "VehiclesScreen composed successfully")
}

@Composable
fun RowScope.VehicleTypeChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    cardColor: Color,
    textColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) selectedColor else cardColor,
        shadowElevation = if (isSelected) 0.dp else 1.dp,
        tonalElevation = if (isSelected) 0.dp else 1.dp
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
                tint = if (isSelected) {
                    if (isDarkTheme) Color.Black else Color.White
                } else {
                    textColor
                },
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    if (isDarkTheme) Color.Black else Color.White
                } else {
                    textColor
                }
            )
        }
    }
}

// Simple dialog to avoid dependencies
@Composable
fun SimpleAddVehicleDialog(
    vehicleType: String,  // ⭐ Accept dynamic vehicle type
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vehicleType) },  // ⭐ Show dynamic title
        text = { Text("Vehicle addition feature coming soon!") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}