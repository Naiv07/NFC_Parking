package com.example.nfc_parking.navigation

object NavRoutes {
    const val AUTH = "auth"
    const val LOADING = "loading"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val PAYMENT = "payment"

    const val BOOKINGS_HISTORY = "bookings_history"
    const val PARKING_DETAIL = "parking_detail/{locationId}"
    const val SELECT_SPACE = "select_space/{locationId}/{locationName}"

    fun parkingDetail(locationId: String) = "parking_detail/$locationId"

    fun selectSpace(locationId: String, locationName: String) =
        "select_space/$locationId/$locationName"
}