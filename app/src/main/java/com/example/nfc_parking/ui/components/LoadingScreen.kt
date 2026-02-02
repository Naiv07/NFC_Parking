package com.example.nfc_parking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.nfc_parking.R
import com.example.nfc_parking.R.raw.loading_dark
import com.example.nfc_parking.R.raw.loading_light
import com.example.nfc_parking.data.ThemeManager
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // 🎨 Color Scheme
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)

    // 🎬 Choose animation based on theme
    val animationRes = if (isDarkTheme) {
        loading_dark  // Dark theme animation
    } else {
        loading_light // Light theme animation
    }

    // Load Lottie composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // ⏱️ Auto-navigate after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000) // Adjust time as needed
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF0F0F0F),
                        Color(0xFF121212)
                    ) else listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE8F4F8),
                        Color(0xFFDCE9F0)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🎬 Lottie Animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp) // Adjust size as needed
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 📝 Loading Text
            Text(
                text = "Getting Ready...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Setting up your parking experience",
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}


