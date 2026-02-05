package com.example.nfc_parking.navigation

/**
 * Navigation routes for the app
 */
object NavRoutes {
    const val AUTH = "auth"
    const val LOADING = "loading"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val VEHICLES = "vehicles"
    const val PAYMENTS = "payments"
    const val EV_CHARGING = "ev_charging"
    const val TERMS = "terms"

    // Booking related routes
    const val BOOKINGS_HISTORY = "bookings_history"
    const val PAYMENT = "payment"

    // ✅ NEW: Alerts/Notifications
    const val ALERTS = "alerts"

    // Routes with parameters
    const val PARKING_DETAIL = "parking_detail/{locationId}"
    const val SELECT_SPACE = "select_space/{locationId}/{locationName}"

    // Helper functions to create routes with parameters
    fun parkingDetail(locationId: String) = "parking_detail/$locationId"
    fun selectSpace(locationId: String, locationName: String) = "select_space/$locationId/$locationName"
}