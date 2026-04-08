package com.example.nfc_parking.ui.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager
import kotlinx.coroutines.delay

@Composable
fun PinEntryScreen(
    onBack: () -> Unit,
    onPinConfirmed: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    var pin by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = true) {
        if (!isProcessing) {
            onBack()
        }
    }

    // Theme colors
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF4285F4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween  // ✅ Distribute evenly
        ) {
            // ===== TOP SECTION =====
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!isProcessing) {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(cardColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }

                    Text(
                        text = "Enter PIN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)  // ✅ Reduced from 140dp
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(55.dp)  // ✅ Reduced from 60dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Confirm Payment",
                    fontSize = 24.sp,  // ✅ Reduced from 26sp
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter your 4-digit PIN",
                    fontSize = 14.sp,
                    color = subtextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "(Use 1234 for demo)",
                    fontSize = 12.sp,
                    color = subtextColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Display (Dots)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        PinDot(
                            isFilled = index < pin.length,
                            accentColor = accentColor,
                            cardColor = cardColor,
                            error = error != null
                        )
                        if (index < 3) Spacer(modifier = Modifier.width(16.dp))  // ✅ Reduced spacing
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error message
                Box(
                    modifier = Modifier.height(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (error != null) {
                        Text(
                            text = error!!,
                            fontSize = 13.sp,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ===== BOTTOM SECTION: Number Pad =====
            NumberPad(
                onNumberClick = { number ->
                    if (pin.length < 4 && !isProcessing) {
                        pin += number
                        error = null

                        // Auto-submit when 4 digits entered
                        if (pin.length == 4) {
                            isProcessing = true
                        }
                    }
                },
                onBackspace = {
                    if (pin.isNotEmpty() && !isProcessing) {
                        pin = pin.dropLast(1)
                        error = null
                    }
                },
                accentColor = accentColor,
                textColor = textColor,
                cardColor = cardColor,
                enabled = !isProcessing
            )
        }
    }

    // Process PIN when entered
    LaunchedEffect(isProcessing) {
        if (isProcessing && pin.length == 4) {
            delay(1500)

            if (pin == "1234") {
                onPinConfirmed()
            } else {
                error = "Incorrect PIN. Try 1234"
                pin = ""
                isProcessing = false
            }
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    accentColor: Color,
    cardColor: Color,
    error: Boolean
) {
    Box(
        modifier = Modifier
            .size(16.dp)  // ✅ Reduced from 18dp
            .background(
                color = when {
                    error -> Color(0xFFEF4444)
                    isFilled -> accentColor
                    else -> cardColor
                },
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = when {
                    error -> Color(0xFFEF4444)
                    isFilled -> accentColor
                    else -> accentColor.copy(alpha = 0.3f)
                },
                shape = CircleShape
            )
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    accentColor: Color,
    textColor: Color,
    cardColor: Color,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)  // ✅ Fixed spacing
    ) {
        // Rows 1-3
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { numbers ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)  // ✅ Fixed spacing
            ) {
                numbers.forEach { number ->
                    NumberButton(
                        number = number,
                        onClick = { onNumberClick(number) },
                        textColor = textColor,
                        cardColor = cardColor,
                        enabled = enabled
                    )
                }
            }
        }

        // Row 4: Empty, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)  // ✅ Fixed spacing
        ) {
            Spacer(modifier = Modifier.size(70.dp))

            NumberButton(
                number = "0",
                onClick = { onNumberClick("0") },
                textColor = textColor,
                cardColor = cardColor,
                enabled = enabled
            )

            IconButton(
                onClick = onBackspace,
                enabled = enabled,
                modifier = Modifier
                    .size(70.dp)
                    .background(cardColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)  // ✅ Reduced from 26dp
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit,
    textColor: Color,
    cardColor: Color,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(70.dp),  // ✅ Good size
        colors = ButtonDefaults.buttonColors(
            containerColor = cardColor,
            disabledContainerColor = cardColor.copy(alpha = 0.5f)
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number,
            fontSize = 24.sp,  // ✅ Reduced from 26sp
            fontWeight = FontWeight.Medium,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}