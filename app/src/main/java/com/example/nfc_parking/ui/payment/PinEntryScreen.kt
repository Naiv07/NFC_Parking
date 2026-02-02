package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.nfc_parking.data.ThemeManager
import kotlinx.coroutines.delay

@Composable
fun PinEntryScreen(
    onBack: () -> Unit,
    onPinConfirmed: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    LaunchedEffect(pin) {
        if (pin.length == 4 && !isLoading) {
            isLoading = true
            delay(2000)
            onPinConfirmed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(cardColor, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                Text(
                    text = "Enter Your PIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Enter your PIN to complete the slot confirmation.",
                fontSize = 14.sp,
                color = subtextColor,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardColor)
                            .border(
                                width = 2.dp,
                                color = if (pin.length > index) accentColor else subtextColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pin.length > index) {
                            Text(
                                text = pin[index].toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔐",
                    fontSize = 120.sp,
                    color = accentColor.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PinNumberPad(
                onNumberClick = { number ->
                    if (pin.length < 4) {
                        pin += number
                    }
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                },
                textColor = textColor,
                cardColor = cardColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = pin.length == 4 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = subtextColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (isDarkTheme) Color.Black else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Confirm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.Black else Color.White
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = accentColor,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
private fun PinNumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    textColor: Color,
    cardColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PinNumberButton("1", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("2", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("3", Modifier.weight(1f), onNumberClick, textColor, cardColor)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PinNumberButton("4", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("5", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("6", Modifier.weight(1f), onNumberClick, textColor, cardColor)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PinNumberButton("7", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("8", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            PinNumberButton("9", Modifier.weight(1f), onNumberClick, textColor, cardColor)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PinNumberButton("0", Modifier.weight(1f), onNumberClick, textColor, cardColor)
            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cardColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "⌫",
                    fontSize = 24.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun PinNumberButton(
    number: String,
    modifier: Modifier,
    onClick: (String) -> Unit,
    textColor: Color,
    cardColor: Color
) {
    Button(
        onClick = { onClick(number) },
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}