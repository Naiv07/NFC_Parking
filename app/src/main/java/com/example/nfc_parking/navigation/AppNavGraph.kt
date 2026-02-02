package com.example.nfc_parking.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = NavRoutes.AUTH,
    preferencesManager: UserPreferencesManager
) {
    val bookingViewModel: BookingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

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

        composable(NavRoutes.LOADING) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController,
                onLocationClick = { location ->
                    navController.navigate(NavRoutes.parkingDetail(location.id))
                }
            )
        }

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
                    onNavigate = { },
                    onBookNow = {
                        val encodedName = URLEncoder.encode(location.name, StandardCharsets.UTF_8.toString())
                        navController.navigate(NavRoutes.selectSpace(location.id, encodedName))
                    }
                )
            }
        }

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

        // 📜 BOOKINGS HISTORY SCREEN ✅ NEW
        composable(NavRoutes.BOOKINGS_HISTORY) {
            BookingsHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

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
                onNavigateToVehicles = { navController.navigate("vehicles") },
                onNavigateToPayments = { navController.navigate("payments") },
                onNavigateToBookings = { navController.navigate(NavRoutes.BOOKINGS_HISTORY) }, // ✅ ADD THIS
                onNavigateToEVCharging = { navController.navigate("ev_charging") },
                onNavigateToTerms = { navController.navigate("terms") },
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

        composable("vehicles") {
            VehiclesScreen(onBack = { navController.popBackStack() })
        }

        composable("payments") { }
        composable("ev_charging") { }
        composable("terms") { }
    }
}

fun getAllParkingLocations(): List<ParkingLocation> {
    return listOf(
        ParkingLocation(
            id = "1",
            name = "Illinois Center",
            address = "111 E Wacker Dr, Chicago",
            distance = "1.4 km",
            duration = "5 min",
            pricePerHour = "$5",
            availableSpots = 24,
            accentColor = Color(0xFF9ACC06)
        ),
        ParkingLocation(
            id = "2",
            name = "AON Center Parking",
            address = "200 E Randolph St, Chicago",
            distance = "2.1 km",
            duration = "8 min",
            pricePerHour = "$6",
            availableSpots = 15,
            accentColor = Color(0xFF00D9FF)
        ),
        ParkingLocation(
            id = "3",
            name = "Millennium Park Garage",
            address = "5 S Columbus Dr, Chicago",
            distance = "0.8 km",
            duration = "3 min",
            pricePerHour = "$8",
            availableSpots = 42,
            accentColor = Color(0xFFFF6B9D)
        ),
        ParkingLocation(
            id = "4",
            name = "Navy Pier Parking",
            address = "600 E Grand Ave, Chicago",
            distance = "3.2 km",
            duration = "12 min",
            pricePerHour = "$10",
            availableSpots = 8,
            accentColor = Color(0xFFFFB800)
        ),
        ParkingLocation(
            id = "5",
            name = "Mag Mile Plaza",
            address = "Water Tower Place, Chicago",
            distance = "1.9 km",
            duration = "7 min",
            pricePerHour = "$7",
            availableSpots = 31,
            accentColor = Color(0xFF9D4EDD)
        ),
        ParkingLocation(
            id = "6",
            name = "Grant Park Underground",
            address = "337 E Randolph St, Chicago",
            distance = "1.1 km",
            duration = "4 min",
            pricePerHour = "$6",
            availableSpots = 19,
            accentColor = Color(0xFF05B273)
        )
    )
}