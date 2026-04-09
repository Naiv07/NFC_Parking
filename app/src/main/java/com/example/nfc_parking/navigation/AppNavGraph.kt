package com.example.nfc_parking.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nfc_parking.data.UserPreferencesManager
import com.example.nfc_parking.ui.auth.AuthScreen
import com.example.nfc_parking.viewmodel.AuthViewModel
import com.example.nfc_parking.ui.home.HomeScreen
import com.example.nfc_parking.ui.home.ParkingLocation
import com.example.nfc_parking.ui.components.LoadingScreen
import com.example.nfc_parking.ui.profile.ProfileScreen
import com.example.nfc_parking.viewmodel.ProfileViewModel
import com.example.nfc_parking.viewmodel.BookingViewModel
import com.example.nfc_parking.ui.vehicles.VehiclesScreen
import com.example.nfc_parking.ui.parkingdetail.ParkingLocationDetailScreen
import com.example.nfc_parking.ui.selectspace.SelectParkingSpaceScreen
import com.example.nfc_parking.ui.payment.PaymentFlowStateBased
import androidx.compose.ui.graphics.Color
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.nfc_parking.ui.bookings.BookingsHistoryScreen
import com.example.nfc_parking.ui.alerts.AlertsScreen
import com.example.nfc_parking.ui.permissions.PermissionScreen
import com.example.nfc_parking.ui.ticket.ParkingTicketScreen
import com.example.nfc_parking.data.BookingManager
import com.example.nfc_parking.ui.profile.EVChargingScreen
import com.example.nfc_parking.ui.profile.PaymentReceiptsScreen
import com.example.nfc_parking.ui.profile.TermsConditionsScreen


@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = NavRoutes.PERMISSIONS,
    preferencesManager: UserPreferencesManager
) {
    val bookingViewModel: BookingViewModel = viewModel()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // PERMISSIONS SCREEN (First)
        composable(NavRoutes.PERMISSIONS) {
            PermissionScreen(
                onAllPermissionsGranted = {
                    preferencesManager.setPermissionsCompleted(true)
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        // AUTHENTICATION SCREEN (Second - has Google Sign-In)
        composable(NavRoutes.AUTH) {
            val viewModel: AuthViewModel = viewModel()
            AuthScreen(
                viewModel = viewModel,
                preferencesManager = preferencesManager,
                onAuthSuccess = {
                    navController.navigate(NavRoutes.LOADING) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // LOADING SCREEN
        composable(NavRoutes.LOADING) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        // HOME SCREEN
        composable(NavRoutes.HOME) {
            LaunchedEffect(Unit) {
                bookingViewModel.clearBooking()
            }
            HomeScreen(
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController,
                onLocationClick = { location ->
                    navController.navigate(NavRoutes.parkingDetail(location.id))
                },
                onNavigateToBookings = {
                    android.util.Log.d("AppNavGraph", "Navigating to bookings!")
                    navController.navigate(NavRoutes.BOOKINGS_HISTORY)
                }
            )
        }

        // PARKING DETAIL SCREEN
        composable(
            route = NavRoutes.PARKING_DETAIL,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            val location = getAllParkingLocations().find { it.id == locationId }

            if (location != null) {
                ParkingLocationDetailScreen(
                    location = location,
                    onBack = { navController.navigateUp() },
                    onBookNow = {
                        val encodedName = URLEncoder.encode(location.name, StandardCharsets.UTF_8.toString())
                        navController.navigate(NavRoutes.selectSpace(location.id, encodedName))
                    }
                )
            }
        }

        // SELECT PARKING SPACE SCREEN
        composable(
            route = NavRoutes.SELECT_SPACE,
            arguments = listOf(
                navArgument("locationId") { type = NavType.StringType },
                navArgument("locationName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            val locationName = backStackEntry.arguments?.getString("locationName") ?: ""

            SelectParkingSpaceScreen(
                locationId = locationId,
                locationName = locationName,
                onBack = { navController.popBackStack() },
                onNavigateToPayment = { booking ->
                    bookingViewModel.setBooking(booking)
                    navController.navigate(NavRoutes.PAYMENT)
                }
            )
        }

        // PAYMENT SCREEN
        composable(NavRoutes.PAYMENT) {
            val booking by bookingViewModel.currentBooking

            if (booking != null) {
                PaymentFlowStateBased(
                    booking = booking!!,
                    onBackToMain = {
                        bookingViewModel.clearBooking()
                        navController.popBackStack(NavRoutes.HOME, false)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // BOOKINGS HISTORY SCREEN
        composable(NavRoutes.BOOKINGS_HISTORY) {
            BookingsHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // PARKING TICKET SCREEN
        composable(NavRoutes.PARKING_TICKET) {
            val booking by bookingViewModel.currentBooking

            if (booking != null) {
                var liveBooking by remember { mutableStateOf(booking) }

                LaunchedEffect(booking!!.bookingId) {
                    while (true) {
                        val updated = BookingManager.getBooking(booking!!.bookingId)
                        if (updated != null) {
                            liveBooking = updated
                            bookingViewModel.setBooking(updated)
                        }
                        delay(5000)
                    }
                }

                ParkingTicketScreen(
                    booking = liveBooking!!,
                    onBack = {
                        bookingViewModel.clearBooking()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            }
        }

        // ALERTS/NOTIFICATIONS SCREEN
        composable(NavRoutes.ALERTS) {
            AlertsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTicket = { bookingId ->
                    scope.launch {
                        android.util.Log.d("Navigation", "Loading booking: $bookingId")
                        val booking = BookingManager.getBooking(bookingId)
                        if (booking != null) {
                            android.util.Log.d("Navigation", "Booking found, navigating to ticket")
                            bookingViewModel.setBooking(booking)
                            navController.navigate(NavRoutes.PARKING_TICKET)
                        } else {
                            android.util.Log.e("Navigation", "Booking not found: $bookingId")
                        }
                    }
                }
            )
        }

        // PROFILE SCREEN
        composable(NavRoutes.PROFILE) {
            val viewModel: ProfileViewModel = viewModel()
            val userName by viewModel.userName
            val userEmail by viewModel.userEmail

            LaunchedEffect(Unit) {
                viewModel.loadUserData(preferencesManager)
            }

            ProfileScreen(
                userEmail = userEmail,
                userName = userName,
                profileImageUrl = null,
                headerImageUrl = null,
                onNavigateToVehicles = { navController.navigate(NavRoutes.VEHICLES) },
                onNavigateToPayments = { navController.navigate(NavRoutes.PAYMENT_RECEIPTS) },
                onNavigateToEVCharging = { navController.navigate(NavRoutes.EV_CHARGING) },
                onNavigateToTerms = { navController.navigate(NavRoutes.TERMS_CONDITIONS) },
                onLogout = {
                    viewModel.logout(preferencesManager) {
                        navController.navigate(NavRoutes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // VEHICLES SCREEN
        composable(NavRoutes.VEHICLES) {
            VehiclesScreen(onBack = { navController.popBackStack() })
        }

        // PAYMENT & RECEIPTS SCREEN
        composable(NavRoutes.PAYMENT_RECEIPTS) {
            PaymentReceiptsScreen(onBack = { navController.popBackStack() })
        }

        // EV CHARGING SCREEN
        composable(NavRoutes.EV_CHARGING) {
            EVChargingScreen(onBack = { navController.popBackStack() })
        }

        // TERMS & CONDITIONS SCREEN
        composable(NavRoutes.TERMS_CONDITIONS) {
            TermsConditionsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun getAllParkingLocations(): List<ParkingLocation> {
    return listOf(
        ParkingLocation("1", "Indiranagar Metro Parking", "100 Feet Road, Indiranagar, Bangalore", "1.2 km", "4 min", "Rs.20", 35, 50, Color(0xFF9ACC06), 12.9716, 77.6412),
        ParkingLocation("2", "MG Road Plaza", "Brigade Road, MG Road, Bangalore", "0.8 km", "3 min", "Rs.30", 28, 40, Color(0xFF00D9FF), 12.9716, 77.6003),
        ParkingLocation("3", "Koramangala Forum Mall", "Hosur Main Road, Koramangala, Bangalore", "2.5 km", "9 min", "Rs.25", 42, 60, Color(0xFFFF6B9D), 12.9352, 77.6245),
        ParkingLocation("4", "Electronic City Tech Park", "Hosur Road, Electronic City, Bangalore", "18.2 km", "35 min", "Rs.15", 65, 100, Color(0xFFFFB800), 12.8456, 77.6603),
        ParkingLocation("5", "Whitefield IT Hub", "ITPL Main Road, Whitefield, Bangalore", "15.8 km", "32 min", "Rs.18", 50, 80, Color(0xFF9D4EDD), 12.9698, 77.7499),
        ParkingLocation("6", "Bandra Kurla Complex", "BKC, Bandra East, Mumbai", "8.5 km", "22 min", "Rs.40", 38, 60, Color(0xFF05B273), 19.0596, 72.8656),
        ParkingLocation("7", "Andheri Metro Station", "Western Express Highway, Andheri, Mumbai", "12.3 km", "28 min", "Rs.35", 45, 70, Color(0xFFE63946), 19.1197, 72.8464),
        ParkingLocation("8", "Lower Parel Mall", "High Street Phoenix, Lower Parel, Mumbai", "6.2 km", "18 min", "Rs.50", 25, 40, Color(0xFFF77F00), 19.0095, 72.8295),
        ParkingLocation("9", "Marine Drive Plaza", "Netaji Subhash Road, Marine Drive, Mumbai", "4.8 km", "15 min", "Rs.60", 18, 30, Color(0xFF06FFA5), 18.9432, 72.8236),
        ParkingLocation("10", "Powai IT Park", "Hiranandani Gardens, Powai, Mumbai", "10.5 km", "25 min", "Rs.28", 55, 80, Color(0xFF4361EE), 19.1197, 72.9059),
        ParkingLocation("11", "Connaught Place Center", "Inner Circle, CP, New Delhi", "3.2 km", "12 min", "Rs.35", 40, 60, Color(0xFFFF006E), 28.6315, 77.2167),
        ParkingLocation("12", "Nehru Place Metro", "Nehru Place, South Delhi", "8.8 km", "20 min", "Rs.25", 52, 80, Color(0xFF8338EC), 28.5494, 77.2501),
        ParkingLocation("13", "Cyber Hub Gurgaon", "DLF Cyber City, Gurgaon", "22.5 km", "40 min", "Rs.30", 70, 120, Color(0xFFFB5607), 28.4942, 77.0892),
        ParkingLocation("14", "Saket Select City", "District Center, Saket, Delhi", "12.2 km", "28 min", "Rs.40", 35, 50, Color(0xFF3A86FF), 28.5244, 77.2066),
        ParkingLocation("15", "Rajiv Chowk Metro", "Barakhamba Road, Rajiv Chowk, Delhi", "2.8 km", "10 min", "Rs.30", 30, 45, Color(0xFFFFBE0B), 28.6328, 77.2197),
        ParkingLocation("16", "HITEC City Tech Hub", "HITEC City, Madhapur, Hyderabad", "15.2 km", "30 min", "Rs.22", 60, 100, Color(0xFF06FFA5), 17.4435, 78.3772),
        ParkingLocation("17", "Banjara Hills Mall", "Road No 1, Banjara Hills, Hyderabad", "8.5 km", "20 min", "Rs.28", 38, 60, Color(0xFFFF006E), 17.4239, 78.4738),
        ParkingLocation("18", "Gachibowli IT Park", "ORR, Gachibowli, Hyderabad", "18.8 km", "35 min", "Rs.20", 72, 120, Color(0xFF4CC9F0), 17.4399, 78.3489),
        ParkingLocation("19", "Begumpet Airport", "Begumpet, Hyderabad", "5.2 km", "15 min", "Rs.35", 45, 70, Color(0xFF9D4EDD), 17.4515, 78.4673),
        ParkingLocation("20", "Kukatpally Hub", "KPHB Colony, Kukatpally, Hyderabad", "12.5 km", "28 min", "Rs.18", 55, 80, Color(0xFF06D6A0), 17.4948, 78.3914),
        ParkingLocation("21", "Hinjewadi IT Park", "Phase 1, Hinjewadi, Pune", "20.5 km", "38 min", "Rs.15", 80, 150, Color(0xFFFFC300), 18.5912, 73.7389),
        ParkingLocation("22", "Koregaon Park Plaza", "North Main Road, Koregaon Park, Pune", "4.8 km", "15 min", "Rs.30", 32, 50, Color(0xFF06FFA5), 18.5362, 73.8958),
        ParkingLocation("23", "Viman Nagar Airport", "Airport Road, Viman Nagar, Pune", "8.2 km", "20 min", "Rs.40", 40, 60, Color(0xFF4361EE), 18.5679, 73.9143),
        ParkingLocation("24", "Aundh IT Hub", "Aundh-Baner Road, Aundh, Pune", "10.5 km", "25 min", "Rs.22", 48, 70, Color(0xFFFF006E), 18.5642, 73.8077),
        ParkingLocation("25", "Deccan Gymkhana", "FC Road, Deccan, Pune", "6.2 km", "18 min", "Rs.25", 35, 50, Color(0xFF8338EC), 18.5089, 73.8429)
    )
}