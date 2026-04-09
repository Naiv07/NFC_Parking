package com.example.nfc_parking.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfc_parking.data.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
    onBack: () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme

    // Colors
    val accentColor = if (isDarkTheme) Color(0xFF39FF14) else Color(0xFF1E3A8A)
    val bgColor = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF8F9FA)
    val cardColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Terms of Service", "Privacy Policy")

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Terms & Conditions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = accentColor
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) accentColor else subtextColor,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    TermsOfServiceContent(
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                } else {
                    PrivacyPolicyContent(
                        isDarkTheme = isDarkTheme,
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtextColor = subtextColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Last updated: March 2026",
                    fontSize = 12.sp,
                    color = subtextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun TermsOfServiceContent(
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    // Header Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, null, tint = accentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Terms of Service", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Text("Please read these terms carefully", fontSize = 13.sp, color = subtextColor)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TermsSectionCard(
        icon = Icons.Default.Handshake,
        title = "1. Acceptance of Terms",
        content = "By downloading, installing, or using the Smart Parking Management application (\"App\"), you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use the App. These terms constitute a legally binding agreement between you and Smart Parking Management.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.PersonOutline,
        title = "2. User Accounts",
        content = "You must register an account to use the App. You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account. You must provide accurate and complete information during registration. You must notify us immediately of any unauthorized use of your account. You must be at least 18 years old to create an account.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.LocalParking,
        title = "3. Parking Services",
        content = "The App provides a platform for users to search, reserve, and pay for parking spaces. Parking slot availability is displayed in real time but is subject to change. Bookings are confirmed only after successful payment. Parking duration begins at the booked start time regardless of actual arrival. Overstaying beyond the booked duration may result in additional charges. The App does not own or operate parking facilities directly.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Payment,
        title = "4. Payments & Refunds",
        content = "All payments are processed securely through integrated payment gateways. Prices displayed are inclusive of applicable taxes unless stated otherwise. Refunds for cancelled bookings are processed to the original payment method or wallet within 3-5 business days. No refunds will be issued for no-shows or late cancellations (within 15 minutes of booking start time). Wallet recharges are non-refundable once credited.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Cancel,
        title = "5. Cancellation Policy",
        content = "Bookings can be cancelled free of charge up to 15 minutes before the scheduled start time. Cancellations made within 15 minutes of the start time will incur a cancellation fee of 25% of the booking amount. No refund is provided for cancellations made after the booking start time. Refunds are credited to your in-app wallet immediately upon eligible cancellation.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Block,
        title = "6. Prohibited Activities",
        content = "Users must not misuse the App by creating fake accounts or providing false information, attempting to manipulate parking slot availability, using automated tools or bots to interact with the App, engaging in fraudulent payment activities, transferring bookings to unauthorized third parties, or attempting to reverse-engineer or tamper with the App's functionality.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Warning,
        title = "7. Limitation of Liability",
        content = "Smart Parking Management shall not be liable for any vehicle damage, theft, or loss occurring at parking facilities. We do not guarantee uninterrupted or error-free service. Our total liability is limited to the amount paid by the user for the specific booking in question. We are not responsible for actions of parking facility operators or third-party service providers.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Gavel,
        title = "8. Governing Law",
        content = "These Terms shall be governed by and construed in accordance with the laws of India. Any disputes arising from the use of this App shall be subject to the exclusive jurisdiction of the courts in Bengaluru, Karnataka.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
}

@Composable
fun PrivacyPolicyContent(
    isDarkTheme: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    // Header Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Shield, null, tint = accentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Privacy Policy", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Text("How we collect, use, and protect your data", fontSize = 13.sp, color = subtextColor)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TermsSectionCard(
        icon = Icons.Default.DataUsage,
        title = "1. Information We Collect",
        content = "We collect information you provide during registration such as your name, email address, phone number, and vehicle details. We also collect usage data including booking history, payment records, location data (with your permission), and device information for app performance optimization. NFC interaction data is collected solely for parking check-in/check-out functionality.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Settings,
        title = "2. How We Use Your Information",
        content = "Your information is used to provide and improve parking services, process bookings and payments, send booking confirmations and alerts, personalize your app experience, ensure account security and prevent fraud, and communicate service updates and promotional offers (with your consent). We do not sell your personal data to third parties.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Storage,
        title = "3. Data Storage & Security",
        content = "All user data is stored securely using Firebase with industry-standard encryption. We implement Firebase Authentication for secure user access. Payment information is processed through PCI-DSS compliant payment gateways and is never stored on our servers. We use HTTPS for all data transmission. Regular security audits are conducted to ensure data protection.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Share,
        title = "4. Data Sharing",
        content = "We may share your information with parking facility operators (limited to booking details for verification), payment gateway providers (for transaction processing), analytics services (anonymized data for app improvement), and law enforcement agencies (when required by law). We do not share your personal information for marketing purposes with third parties.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.LocationOn,
        title = "5. Location Data",
        content = "The App collects location data to show nearby parking facilities and provide navigation to booked parking slots. Location access is requested only when needed and can be disabled in your device settings. We do not track your location when the App is not in use. Historical location data is not stored beyond what is necessary for booking records.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.ManageAccounts,
        title = "6. Your Rights",
        content = "You have the right to access, update, or delete your personal information at any time through your profile settings. You can request a copy of all data we hold about you. You can opt out of promotional communications. You can delete your account, which will remove all associated personal data. For any data-related requests, please contact us through the App's support section.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.ChildCare,
        title = "7. Children's Privacy",
        content = "The App is not intended for use by individuals under the age of 18. We do not knowingly collect personal information from children. If you believe a child has provided us with personal data, please contact us and we will take steps to delete such information promptly.",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
    Spacer(modifier = Modifier.height(12.dp))

    TermsSectionCard(
        icon = Icons.Default.Email,
        title = "8. Contact Us",
        content = "If you have any questions about these Terms or our Privacy Policy, please contact us at:\n\nEmail: support@smartparking.app\nPhone: +91-80-XXXX-XXXX\nAddress: Presidency College, Kempapura, Hebbal, Bengaluru - 560024",
        accentColor = accentColor, cardColor = cardColor, textColor = textColor, subtextColor = subtextColor
    )
}

@Composable
fun TermsSectionCard(
    icon: ImageVector,
    title: String,
    content: String,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                content,
                fontSize = 13.sp,
                color = subtextColor,
                lineHeight = 20.sp
            )
        }
    }
}