package com.example.nfc_parking.data

import androidx.compose.runtime.mutableStateListOf
import com.example.nfc_parking.ui.home.ParkingLocation

object SavedLocationsManager {
    private val _savedLocations = mutableStateListOf<ParkingLocation>()
    val savedLocations: List<ParkingLocation> get() = _savedLocations

    fun toggleSave(location: ParkingLocation) {
        if (isSaved(location)) {
            _savedLocations.removeAll { it.id == location.id }
        } else {
            _savedLocations.add(location)
        }
    }

    fun isSaved(location: ParkingLocation): Boolean {
        return _savedLocations.any { it.id == location.id }
    }
}