package com.example.nfc_parking.ui.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    // Required permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.NFC
        )
    )

    // Check if all permissions are granted
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onAllPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon/Logo
        Icon(
            imageVector = Icons.Default.LocalParking,
            contentDescription = "App Logo",
            tint = accentColor,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "NFC Parking",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Smart parking made simple",
            fontSize = 16.sp,
            color = subtextColor
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Required Permissions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permission Cards
        PermissionCard(
            icon = Icons.Default.LocationOn,
            title = "Location Access",
            description = "Find nearby parking spots",
            isGranted = permissionsState.permissions.any {
                it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
            },
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            icon = Icons.Default.Nfc,
            title = "NFC Access",
            description = "Scan parking tags for quick entry",
            isGranted = permissionsState.permissions.any {
                it.permission == Manifest.permission.NFC && it.status.isGranted
            },
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Grant Permissions Button
        Button(
            onClick = {
                permissionsState.launchMultiplePermissionRequest()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor
            ),
            enabled = !permissionsState.allPermissionsGranted
        ) {
            Text(
                text = if (permissionsState.allPermissionsGranted) "All Permissions Granted ✓" else "Grant Permissions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.Black else Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We respect your privacy. These permissions help provide the best parking experience.",
            fontSize = 12.sp,
            color = subtextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else subtextColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = subtextColor
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}