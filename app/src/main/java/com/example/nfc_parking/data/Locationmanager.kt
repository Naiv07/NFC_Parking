package com.example.nfc_parking.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)

object LocationManager {

    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Initialize location client
     */
    fun initialize(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Check if location permissions are granted
     */
    private fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Check if location services are enabled
     */
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get current location with multiple fallback strategies
     */
    suspend fun getCurrentLocation(context: Context): Result<UserLocation> {
        // Check permissions
        if (!hasLocationPermission(context)) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        // Check if location is enabled
        if (!isLocationEnabled(context)) {
            return Result.failure(Exception("GPS is disabled. Please enable location services."))
        }

        return try {
            val client = fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(context)

            // Strategy 1: Try to get last known location (fastest)
            val lastLocation = getLastKnownLocation(client)
            if (lastLocation != null) {
                return Result.success(lastLocation)
            }

            // Strategy 2: Request fresh location with timeout
            val freshLocation = requestFreshLocationWithTimeout(client)
            if (freshLocation != null) {
                return Result.success(freshLocation)
            }

            // Strategy 3: Use default location (Bangalore) as fallback
            Result.success(getDefaultLocation())

        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            // Return default location instead of error
            Result.success(getDefaultLocation())
        }
    }

    /**
     * Get last known location (cached)
     */
    private suspend fun getLastKnownLocation(client: FusedLocationProviderClient): UserLocation? {
        return withTimeoutOrNull(3000L) {
            suspendCancellableCoroutine { continuation ->
                try {
                    client.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val userLocation = UserLocation(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy
                                )
                                continuation.resume(userLocation)
                            } else {
                                continuation.resume(null)
                            }
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                } catch (e: SecurityException) {
                    continuation.resume(null)
                }
            }
        }
    }

    /**
     * Request fresh location with timeout
     */
    private suspend fun requestFreshLocationWithTimeout(client: FusedLocationProviderClient): UserLocation? {
        return withTimeoutOrNull(10000L) { // 10 second timeout
            suspendCancellableCoroutine { continuation ->
                var resumed = false

                try {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        5000L
                    ).apply {
                        setMinUpdateIntervalMillis(2000L)
                        setMaxUpdates(1)
                    }.build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            if (!resumed) {
                                resumed = true
                                val location = result.lastLocation
                                if (location != null) {
                                    val userLocation = UserLocation(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        accuracy = location.accuracy
                                    )
                                    continuation.resume(userLocation)
                                } else {
                                    continuation.resume(null)
                                }
                                client.removeLocationUpdates(this)
                            }
                        }
                    }

                    continuation.invokeOnCancellation {
                        client.removeLocationUpdates(locationCallback)
                    }

                    client.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                } catch (e: SecurityException) {
                    if (!resumed) {
                        resumed = true
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    /**
     * Get default location (Bangalore city center)
     */
    private fun getDefaultLocation(): UserLocation {
        return UserLocation(
            latitude = 12.9716,  // Bangalore
            longitude = 77.5946,
            accuracy = 1000f  // 1km accuracy
        )
    }

    /**
     * Get continuous location updates as Flow
     */
    fun getLocationUpdates(context: Context): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val client = fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Update every 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val userLocation = UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )
                    trySend(userLocation)
                }
            }
        }

        try {
            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Calculate distance between two points in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> {
                val meters = (distanceKm * 1000).toInt()
                "$meters m"
            }
            else -> {
                String.format("%.1f km", distanceKm)
            }
        }
    }

    /**
     * Estimate travel time (assuming average speed)
     */
    fun estimateTravelTime(distanceKm: Double, speedKmh: Double = 40.0): String {
        val timeHours = distanceKm / speedKmh
        val timeMinutes = (timeHours * 60).toInt()

        return when {
            timeMinutes < 1 -> "< 1 min"
            timeMinutes < 60 -> "$timeMinutes min"
            else -> {
                val hours = timeMinutes / 60
                val mins = timeMinutes % 60
                "${hours}h ${mins}m"
            }
        }
    }
}