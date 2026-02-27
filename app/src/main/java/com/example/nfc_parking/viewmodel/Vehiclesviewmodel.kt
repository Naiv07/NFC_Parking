package com.example.nfc_parking.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.nfc_parking.ui.vehicles.Vehicle
import com.example.nfc_parking.ui.vehicles.VehicleType

class VehiclesViewModel : ViewModel() {

    // List of user's vehicles
    val vehicles = mutableStateListOf<Vehicle>()

    // Add a new vehicle
    fun addVehicle(vehicle: Vehicle) {
        vehicles.add(vehicle)
    }

    // Remove a vehicle
    fun removeVehicle(vehicleId: String) {
        vehicles.removeIf { it.id == vehicleId }
    }

    // Get vehicles by type
    fun getVehiclesByType(type: VehicleType): List<Vehicle> {
        return vehicles.filter { it.vehicleType == type }
    }

    // Update vehicle
    fun updateVehicle(vehicleId: String, updatedVehicle: Vehicle) {
        val index = vehicles.indexOfFirst { it.id == vehicleId }
        if (index != -1) {
            vehicles[index] = updatedVehicle
        }
    }

    // Load vehicles from database/preferences (implement later)
    fun loadVehicles() {
        // TODO: Load from Firebase or local database
    }
}