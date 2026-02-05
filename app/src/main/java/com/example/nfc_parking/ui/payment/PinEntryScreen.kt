package com.example.nfc_parking.ui.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    // ✅ CRITICAL FIX: Intercept back press to prevent navigation pop
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
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
                    .size(100.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Confirm Payment",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
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

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Display (Dots)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    if (index < 3) Spacer(modifier = Modifier.width(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            // Number Pad
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

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if (pin.length == 4) {
                        isProcessing = true
                    } else {
                        error = "Please enter 4 digits"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing && pin.length == 4,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Confirm Payment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Process PIN when entered
    LaunchedEffect(isProcessing) {
        if (isProcessing && pin.length == 4) {
            delay(1500) // Simulate verification

            // Demo PIN is "1234"
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
            .size(18.dp)
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rows 1-3
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { numbers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
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
                    modifier = Modifier.size(26.dp)
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
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cardColor,
            disabledContainerColor = cardColor.copy(alpha = 0.5f)
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}