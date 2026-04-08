package com.example.nfc_parking.data

import androidx.compose.runtime.mutableStateListOf
import com.example.nfc_parking.ui.vehicles.Vehicle
import com.example.nfc_parking.ui.vehicles.VehicleType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Vehicle(
    val id: String = "",
    val userId: String = "",
    val vehicleType: VehicleType = VehicleType.CAR,
    val vehicleName: String = "",        // e.g., "Honda City"
    val model: String = "",               // e.g., "2020"
    val licensePlate: String = "",        // e.g., "KA-01-AB-1234"
    val color: String = "",               // e.g., "Red"
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class VehicleType {
    CAR,
    BIKE,
    EV,
    TRUCK
}

object VehicleManager {
    private val vehiclesCollection = FirebaseFirestore.getInstance().collection("vehicles")
    private val _vehicles = mutableStateListOf<Vehicle>()
    val vehicles: List<Vehicle> get() = _vehicles.toList()

    /**
     * Load user's vehicles
     */
    suspend fun loadUserVehicles(): Result<List<Vehicle>> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val snapshot = vehiclesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val vehicleList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
            }

            _vehicles.clear()
            _vehicles.addAll(vehicleList)

            android.util.Log.d("VehicleManager", "Loaded ${vehicleList.size} vehicles")
            Result.success(vehicleList)
        } catch (e: Exception) {
            android.util.Log.e("VehicleManager", "Failed to load vehicles: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Add a new vehicle
     */
    suspend fun addVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val newVehicle = vehicle.copy(
                userId = userId,
                id = vehiclesCollection.document().id
            )

            vehiclesCollection.document(newVehicle.id)
                .set(newVehicle)
                .await()

            _vehicles.add(newVehicle)

            android.util.Log.d("VehicleManager", "Vehicle added: ${newVehicle.licensePlate}")
            Result.success(newVehicle)
        } catch (e: Exception) {
            android.util.Log.e("VehicleManager", "Failed to add vehicle: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update a vehicle
     */
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> {
        return try {
            vehiclesCollection.document(vehicle.id)
                .set(vehicle)
                .await()

            val index = _vehicles.indexOfFirst { it.id == vehicle.id }
            if (index != -1) {
                _vehicles[index] = vehicle
            }

            android.util.Log.d("VehicleManager", "Vehicle updated: ${vehicle.licensePlate}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VehicleManager", "Failed to update vehicle: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a vehicle
     */
    suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return try {
            vehiclesCollection.document(vehicleId)
                .delete()
                .await()

            _vehicles.removeAll { it.id == vehicleId }

            android.util.Log.d("VehicleManager", "Vehicle deleted: $vehicleId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VehicleManager", "Failed to delete vehicle: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Set default vehicle
     */
    suspend fun setDefaultVehicle(vehicleId: String): Result<Unit> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            // Remove default from all vehicles
            val batch = FirebaseFirestore.getInstance().batch()

            vehicles.forEach { vehicle ->
                batch.update(
                    vehiclesCollection.document(vehicle.id),
                    "isDefault",
                    vehicle.id == vehicleId
                )
            }

            batch.commit().await()

            // Update local list
            _vehicles.replaceAll { vehicle ->
                vehicle.copy(isDefault = vehicle.id == vehicleId)
            }

            android.util.Log.d("VehicleManager", "Default vehicle set: $vehicleId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VehicleManager", "Failed to set default: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get default vehicle
     */
    fun getDefaultVehicle(): Vehicle? {
        return vehicles.firstOrNull { it.isDefault }
    }
}