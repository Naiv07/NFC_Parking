package com.example.nfc_parking.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.PaymentManager
import com.example.nfc_parking.data.PaymentType
import com.example.nfc_parking.data.SavedCard
import com.example.nfc_parking.data.ThemeManager
import com.example.nfc_parking.data.UpiId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.nfc_parking.data.SavedUpiId
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun PaymentMethodScreen(
    onBack: () -> Unit,
    onPaymentSelected: () -> Unit
) {

    // ✅ ADD THIS LINE - Prevents Android from popping entire payment flow
    androidx.activity.compose.BackHandler(enabled = true) {
        onBack()
    }
    val isDarkTheme by ThemeManager.isDarkTheme
    var showAddCard by remember { mutableStateOf(false) }
    var showAddUpi by remember { mutableStateOf(false) }
    var selectedPaymentType by remember { mutableStateOf<PaymentType?>(null) }

    // 🎨 Color Scheme
    val backgroundColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)

    val savedCards = PaymentManager.savedCards
    val savedUpiIds = PaymentManager.savedUpiIds

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
                    .padding(20.dp),
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
                    text = "Payment Method",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                IconButton(
                    onClick = { /* Settings */ },
                    modifier = Modifier
                        .background(cardColor, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 📱 CONTENT
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Title
                item {
                    Text(
                        text = "Choose your payment method",
                        fontSize = 14.sp,
                        color = subtextColor
                    )
                }

                // 💳 Payment Method Icons Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mastercard/Debit/Credit
                        PaymentIconButton(
                            icon = Icons.Default.CreditCard,
                            backgroundColor = Color(0xFFEB001B),
                            iconColor = Color.White,
                            isSelected = selectedPaymentType == PaymentType.DEBIT_CARD ||
                                    selectedPaymentType == PaymentType.CREDIT_CARD,
                            onClick = {
                                if (selectedPaymentType == PaymentType.DEBIT_CARD) {
                                    selectedPaymentType = null
                                } else {
                                    selectedPaymentType = PaymentType.DEBIT_CARD
                                }
                            }
                        )

                        // UPI
                        PaymentIconButton(
                            icon = Icons.Default.AccountBalance,
                            backgroundColor = Color(0xFF097969),
                            iconColor = Color.White,
                            isSelected = selectedPaymentType == PaymentType.UPI,
                            onClick = {
                                if (selectedPaymentType == PaymentType.UPI) {
                                    selectedPaymentType = null
                                } else {
                                    selectedPaymentType = PaymentType.UPI
                                }
                            }
                        )

                        // GPay (use Payment icon instead of Google)
                        PaymentIconButton(
                            icon = Icons.Default.Payment,
                            backgroundColor = Color(0xFF4285F4),
                            iconColor = Color.White,
                            isSelected = selectedPaymentType == PaymentType.GPAY,
                            onClick = {
                                if (selectedPaymentType == PaymentType.GPAY) {
                                    selectedPaymentType = null
                                } else {
                                    selectedPaymentType = PaymentType.GPAY
                                }
                            }
                        )

                        // Apple Pay (use Phone icon instead of Apple)
                        PaymentIconButton(
                            icon = Icons.Default.PhoneIphone,
                            backgroundColor = Color.Black,
                            iconColor = Color.White,
                            isSelected = selectedPaymentType == PaymentType.APPLE_PAY,
                            onClick = {
                                if (selectedPaymentType == PaymentType.APPLE_PAY) {
                                    selectedPaymentType = null
                                } else {
                                    selectedPaymentType = PaymentType.APPLE_PAY
                                }
                            }
                        )
                    }
                }

                // 💳 SAVED CARDS
                if (savedCards.isNotEmpty()) {
                    item {
                        Text(
                            text = "Saved Cards",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(savedCards.size) { index ->
                        val card = savedCards[index]
                        SavedCardItem(
                            card = card,
                            isSelected = PaymentManager.selectedCardId == card.id,
                            accentColor = accentColor,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            onClick = {
                                if (PaymentManager.selectedCardId == card.id) {
                                    PaymentManager.selectedCardId = null
                                    PaymentManager.selectedPaymentType = null
                                    selectedPaymentType = null
                                } else {
                                    PaymentManager.selectedCardId = card.id
                                    PaymentManager.selectedPaymentType = card.cardType
                                    selectedPaymentType = card.cardType
                                }
                            }
                        )
                    }
                }

                // 🅿️ OTHER PAYMENT METHODS
                item {
                    Text(
                        text = "Other Payment Methods",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    PaymentMethodItem(
                        icon = Icons.Default.Add,
                        title = "Add Credit Card / Debit Card",
                        iconBackgroundColor = accentColor,
                        isSelected = false,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            showAddCard = true
                        }
                    )
                }

                // GPay
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.Payment,
                        title = "Google Pay",
                        iconBackgroundColor = Color(0xFF4285F4),
                        isSelected = selectedPaymentType == PaymentType.GPAY,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.GPAY) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.GPAY
                                PaymentManager.selectedPaymentType = PaymentType.GPAY
                            }
                        }
                    )
                }

// Apple Pay - Replace Apple icon
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.PhoneIphone,
                        title = "Apple Pay",
                        iconBackgroundColor = Color.Black,
                        isSelected = selectedPaymentType == PaymentType.APPLE_PAY,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.APPLE_PAY) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.APPLE_PAY
                                PaymentManager.selectedPaymentType = PaymentType.APPLE_PAY
                            }
                        }
                    )
                }

                // Amazon Pay
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.ShoppingCart,
                        title = "Amazon Pay",
                        iconBackgroundColor = Color(0xFFFF9900),
                        isSelected = selectedPaymentType == PaymentType.AMAZON_PAY,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.AMAZON_PAY) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.AMAZON_PAY
                                PaymentManager.selectedPaymentType = PaymentType.AMAZON_PAY
                            }
                        }
                    )
                }

                // Slice
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.CreditCard,
                        title = "Slice",
                        iconBackgroundColor = Color(0xFF6C63FF),
                        isSelected = selectedPaymentType == PaymentType.SLICE,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.SLICE) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.SLICE
                                PaymentManager.selectedPaymentType = PaymentType.SLICE
                            }
                        }

                    )
                }

                // Simpl
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Simpl",
                        iconBackgroundColor = Color(0xFF00D9A3),
                        isSelected = selectedPaymentType == PaymentType.SIMPL,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.SIMPL) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.SIMPL
                                PaymentManager.selectedPaymentType = PaymentType.SIMPL
                            }
                        }
                    )
                }

                // UPI/VPA
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.AccountBalance,
                        title = "UPI ID / VPA",
                        subtitle = if (savedUpiIds.isNotEmpty()) savedUpiIds.first().upiId else "Add UPI ID",
                        iconBackgroundColor = Color(0xFF097969),
                        isSelected = selectedPaymentType == PaymentType.UPI,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (savedUpiIds.isEmpty()) {
                                showAddUpi = true
                            } else {
                                if (selectedPaymentType == PaymentType.UPI) {
                                    selectedPaymentType = null
                                    PaymentManager.selectedPaymentType = null
                                } else {
                                    selectedPaymentType = PaymentType.UPI
                                    PaymentManager.selectedPaymentType = PaymentType.UPI
                                }
                            }
                        }
                    )
                }

                // Cash
                item {
                    PaymentMethodItem(
                        icon = Icons.Default.Money,
                        title = "Cash",
                        iconBackgroundColor = Color(0xFF4CAF50),
                        isSelected = selectedPaymentType == PaymentType.CASH,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        onClick = {
                            if (selectedPaymentType == PaymentType.CASH) {
                                selectedPaymentType = null
                                PaymentManager.selectedPaymentType = null
                            } else {
                                selectedPaymentType = PaymentType.CASH
                                PaymentManager.selectedPaymentType = PaymentType.CASH
                            }
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // ✅ NEXT BUTTON
        if (selectedPaymentType != null) {
            Button(
                onClick = onPaymentSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White
                )
            }
        }
    }

    // Add Card Bottom Sheet
    if (showAddCard) {
        AddCardBottomSheet(
            onDismiss = { showAddCard = false },
            accentColor = accentColor,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            isDarkTheme = isDarkTheme
        )
    }

    // Add UPI Bottom Sheet
    if (showAddUpi) {
        AddUpiBottomSheet(
            onDismiss = { showAddUpi = false },
            accentColor = accentColor,
            cardColor = cardColor,
            textColor = textColor,
            subtextColor = subtextColor,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun PaymentIconButton(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) backgroundColor.copy(alpha = 1f) else backgroundColor.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) iconColor else iconColor.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun SavedCardItem(
    card: SavedCard,
    isSelected: Boolean,
    accentColor: Color,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(card.gradientColors)
                )
                .then(
                    if (isSelected) Modifier.background(
                        accentColor.copy(alpha = 0.2f)
                    ) else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = card.bankName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "**** **** **** ${card.cardNumber.takeLast(4)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = card.cardholderName,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = card.expiryDate,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconBackgroundColor: Color,
    isSelected: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.1f) else cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
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
                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpiBottomSheet(
    onDismiss: () -> Unit,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    isDarkTheme: Boolean
) {
    var upiId by remember { mutableStateOf("") }

    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Add UPI ID",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                placeholder = { Text("yourname@upi", color = subtextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = surfaceColor,
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (upiId.isNotEmpty()) {
                        val newUpiId: SavedUpiId = SavedUpiId(
                            id = System.currentTimeMillis().toString(),
                            upiId = upiId
                        )
                        PaymentManager.savedUpiIds.add(newUpiId)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun SavedUpiId(id: String, upiId: String) {
    TODO("Not yet implemented")
}

fun formatCardNumber(input: String): String {
    val digits = input.filter { it.isDigit() }
    return digits.chunked(4).joinToString(" ").take(19)
}

fun formatExpiryDate(input: String): String {
    val digits = input.filter { it.isDigit() }
    return when {
        digits.length <= 2 -> digits
        else -> "${digits.take(2)}/${digits.drop(2).take(2)}"
    }
}

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(16)
        val formatted = buildString {
            trimmed.forEachIndexed { index, char ->
                if (index > 0 && index % 4 == 0) {
                    append(' ')
                }
                append(char)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return offset
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                return offset + 3
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                return offset - 3
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val formatted = when {
            trimmed.length <= 2 -> trimmed
            else -> "${trimmed.take(2)}/${trimmed.drop(2)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    else -> offset + 1
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 3 -> 2
                    else -> offset - 1
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(
    onDismiss: () -> Unit,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color,
    isDarkTheme: Boolean
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var saveCardInfo by remember { mutableStateOf(true) }
    var cardType by remember { mutableStateOf(PaymentType.DEBIT_CARD) }

    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)

    var cvvVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }

                    Text(
                        text = "Add New Card",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Credit Card Preview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A8A),
                                        Color(0xFF3B82F6)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Card",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Text(
                                        text = "XOXOBank",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp, 32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFD700))
                                )

                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Contactless",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = if (cardNumber.isEmpty()) {
                                    "XXXX XXXX XXXX XXXX"
                                } else {
                                    formatCardNumber(cardNumber).padEnd(19, 'X')
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "CARDHOLDER",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = cardholderName.ifEmpty { "NAME ON CARD" }.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "EXPIRY DATE",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = if (expiryDate.isEmpty()) "12/28" else {
                                            when {
                                                expiryDate.length <= 2 -> expiryDate
                                                else -> "${expiryDate.take(2)}/${expiryDate.drop(2)}"
                                            }
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Box(modifier = Modifier.size(40.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.CenterStart)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEB001B))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.CenterEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF79E1B))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Card Number
            item {
                Text(
                    text = "Card Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        val digits = it.filter { char -> char.isDigit() }
                        if (digits.length <= 16) {
                            cardNumber = digits
                        }
                    },
                    placeholder = { Text("XXXX XXXX XXXX XXXX", color = subtextColor) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = subtextColor
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true,
                    visualTransformation = CardNumberVisualTransformation()
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Account Holder Name
            item {
                Text(
                    text = "Account Holder Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    placeholder = { Text("Name on Card", color = subtextColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = surfaceColor,
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Expiry Date and CVV Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Expiry Date
                    // Expiry Date
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expiry Date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { newValue ->
                                val digits = newValue.filter { it.isDigit() }
                                if (digits.length <= 4) {
                                    expiryDate = digits
                                }
                            },
                            placeholder = { Text("12/28", color = subtextColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = surfaceColor,
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            singleLine = true,
                            visualTransformation = ExpiryDateVisualTransformation()
                        )
                    }

                    // CVV
                    // CVV
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CVV",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                    cvv = it
                                }
                            },
                            placeholder = { Text("•••", color = subtextColor) },
                            visualTransformation = if (cvvVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { cvvVisible = !cvvVisible }) {
                                    Icon(
                                        imageVector = if (cvvVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (cvvVisible) "Hide CVV" else "Show CVV",
                                        tint = subtextColor
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = surfaceColor,
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Save Card Information Checkbox
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { saveCardInfo = !saveCardInfo },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveCardInfo,
                        onCheckedChange = { saveCardInfo = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accentColor,
                            uncheckedColor = subtextColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Card Information",
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Save Button
            item {
                Button(
                    onClick = {
                        if (cardNumber.isNotEmpty() && cardholderName.isNotEmpty() &&
                            expiryDate.isNotEmpty() && cvv.isNotEmpty()
                        ) {
                            val newCard = SavedCard(
                                id = System.currentTimeMillis().toString(),
                                cardNumber = cardNumber,
                                cardholderName = cardholderName,
                                expiryDate = expiryDate,
                                cvv = cvv,
                                cardType = cardType,
                                bankName = "XOXOXBank",
                                gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                            )
                            PaymentManager.savedCards.add(newCard)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.Black else Color.White
                    )
                }
            }
        }  // ⭐ CLOSE LazyColumn
    }  // ⭐ CLOSE ModalBottomSheet
}  // ⭐ CLOSE Function