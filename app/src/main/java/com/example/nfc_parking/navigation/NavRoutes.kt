package com.example.nfc_parking.navigation

object NavRoutes {
    const val PERMISSIONS = "permissions"
    const val AUTH = "auth"
    const val LOADING = "loading"
    const val BOOKING = "booking"
    const val TICKET = "ticket"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val BOOKINGS_HISTORY = "bookings_history"
    const val ADD_VEHICLE = "add_vehicle"
    const val EDIT_VEHICLE = "edit_vehicle"
    const val ALERTS = "alerts"
    const val BOOKING_CONFIRMATION = "booking_confirmation"  // ✅ Add this
    const val PARKING_TICKET = "parking_ticket"  // ✅ Add this

    const val PARKING_DETAIL = "parking_detail/{locationId}"
    const val SELECT_SPACE = "select_space/{locationId}/{locationName}"
    const val PAYMENT = "payment"

    fun parkingDetail(locationId: String) = "parking_detail/$locationId"
    fun selectSpace(locationId: String, locationName: String) = "select_space/$locationId/$locationName"
}