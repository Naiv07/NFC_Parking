package com.example.nfc_parking.ui.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.navigation.NavRoutes

@Composable
fun ProfileScreen(
    userEmail: String,
    userName: String,
    profileImageUrl: String? = null,
    headerImageUrl: String? = null,

    onNavigateToVehicles: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToEVCharging: () -> Unit,
    onNavigateToTerms: () -> Unit,

    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // 🎨 Color Scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF5F5F5)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val iconBackgroundColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 🎯 HEADER WITH BACKGROUND IMAGE
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Background Header Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                )
                            )
                    ) {
                        if (headerImageUrl != null) {
                            // Use actual image if provided
                            AsyncImage(
                                model = headerImageUrl,
                                contentDescription = "Header Background",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default gradient background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF1E3A8A),
                                                Color(0xFF2563EB),
                                                Color(0xFF3B82F6)
                                            )
                                        )
                                    )
                            )
                        }

                        // Overlay for better text visibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                    }

                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Menu Button
                    IconButton(
                        onClick = { /* TODO: Open menu */ },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    // 👤 PROFILE PICTURE (Overlapping)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, backgroundColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUrl != null) {
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default profile icon
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }

            // 📝 USER INFO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = userName.ifEmpty { "User Name" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 📋 MENU ITEMS

            // ✅ REMOVED "MY BOOKINGS" SECTION

            item {
                ProfileMenuItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Vehicles",
                    iconBackgroundColor = iconBackgroundColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = onNavigateToVehicles
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Payment,
                    title = "Payment & Receipts",
                    iconBackgroundColor = iconBackgroundColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = onNavigateToPayments
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.EvStation,
                    title = "EV Charging",
                    iconBackgroundColor = iconBackgroundColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = onNavigateToEVCharging
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    iconBackgroundColor = iconBackgroundColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = onNavigateToTerms
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 🚪 LOGOUT BUTTON
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    iconBackgroundColor = Color(0xFFEF4444),
                    cardColor = cardColor,
                    textColor = Color(0xFFEF4444),
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconBackgroundColor: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBackgroundColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBackgroundColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f)
            )
        }
    }
}