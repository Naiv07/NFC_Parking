package com.example.nfc_parking.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.room.util.copy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Sealed class for booking operation results with detailed error information
 */
sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Error(val message: String, val exception: Exception? = null) : BookingResult()
}

/**
 * Data class for listener errors
 */
data class ListenerError(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

object BookingManager : DefaultLifecycleObserver {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    // Local cache of booked spaces for quick access
    val bookedSpaces = mutableStateOf<Set<String>>(emptySet())

    // StateFlow for listener errors - UI can observe this
    private val _listenerErrors = MutableStateFlow<ListenerError?>(null)
    val listenerErrors: StateFlow<ListenerError?> = _listenerErrors.asStateFlow()

    // Listener for real-time updates
    private var listener: ListenerRegistration? = null
    private var currentLocationId: String? = null


    /**
     * Start listening for real-time booking updates for a specific location
     * Automatically manages lifecycle to prevent memory leaks
     */
    fun startListening(locationId: String) {
        // Don't restart if already listening to the same location
        if (currentLocationId == locationId && listener != null) {
            return
        }

        listener?.remove() // Remove previous listener if exists
        currentLocationId = locationId

        listener = bookingsCollection
            .whereEqualTo("locationId", locationId)
            .whereIn("status", listOf("active"))  // Only show CONFIRMED bookings as black
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorMessage = "Listen failed: ${error.message}"
                    android.util.Log.e("BookingManager", errorMessage)
                    _listenerErrors.value = ListenerError(errorMessage)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val activeBookings = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Booking::class.java)
                        } catch (e: Exception) {
                            android.util.Log.e("BookingManager", "Failed to parse booking: ${e.message}")
                            null
                        }
                    }.filter { booking ->
                        // Only include bookings that haven't expired
                        booking.endTime > currentTime
                    }

                    // Update local cache - only ACTIVE bookings show as black
                    val newBookedSpaces = activeBookings.map { it.spaceId }.toSet()
                    android.util.Log.d("BookingManager", "Updated booked spaces: $newBookedSpaces")
                    bookedSpaces.value = newBookedSpaces

                    // Clear any previous errors on successful update
                    _listenerErrors.value = null
                }
            }
    }

    /**
     * Stop listening for updates
     */
    fun stopListening() {
        listener?.remove()
        listener = null
        currentLocationId = null
        android.util.Log.d("BookingManager", "Listener stopped")
    }

    /**
     * Lifecycle callback - automatically stop listening when lifecycle is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stopListening()
    }

    /**
     * Reserve a parking space (status = "pending")
     * Uses optimistic locking approach - checks first, then creates atomically
     */
    suspend fun reserveSpace(
        spaceId: String,
        userId: String,
        userName: String,
        locationId: String,
        locationName: String,
        spaceLabel: String,
        durationHours: Int,
        pricePerHour: Double = 4.12
    ): BookingResult {
        return try {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationHours * 60 * 60 * 1000L)

            // Step 1: Check if space is already booked (fast query outside transaction)
            val existingBookings = bookingsCollection
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("locationId", locationId)
                .whereIn("status", listOf("pending", "active"))
                .get()
                .await()

            // Validate no active/pending bookings exist for this space
            val conflictingBooking = existingBookings.documents
                .mapNotNull { it.toObject(Booking::class.java) }
                .firstOrNull { it.endTime > startTime }

            if (conflictingBooking != null) {
                return BookingResult.Error("Space already booked until ${java.text.SimpleDateFormat("HH:mm").format(conflictingBooking.endTime)}")
            }

            // Step 2: Create new PENDING booking with unique ID
            val bookingId = bookingsCollection.document().id
            val newBooking = Booking(
                spaceId = spaceId,
                userId = userId,
                userName = userName,
                startTime = startTime,
                endTime = endTime,
                locationId = locationId,
                locationName = locationName,
                spaceLabel = spaceLabel,
                pricePerHour = pricePerHour,
                totalHours = durationHours,
                totalPrice = pricePerHour * durationHours,
                bookingId = bookingId,
                status = "pending"
            )

            // Step 3: Save to Firestore
            // Using .set() is atomic - if someone else books at same time, one will succeed
            bookingsCollection.document(bookingId).set(newBooking).await()

            android.util.Log.d("BookingManager", "Space reserved (pending): ${newBooking.spaceId}")
            BookingResult.Success(newBooking)

        } catch (e: Exception) {
            val errorMessage = "Reservation failed: ${e.message}"
            android.util.Log.e("BookingManager", errorMessage, e)
            BookingResult.Error(errorMessage, e)
        }
    }

    /**
     * Get number of active bookings for a location
     */
    suspend fun getActiveBookingsCount(locationId: String): Int {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = bookingsCollection
                .whereEqualTo("locationId", locationId)
                .whereEqualTo("status", "active")
                .get()
                .await()

            // Count only non-expired bookings
            val activeCount = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)
            }.count { booking ->
                booking.endTime > currentTime
            }

            android.util.Log.d("BookingManager", "Location $locationId has $activeCount active bookings")
            activeCount
        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Failed to get active bookings: ${e.message}", e)
            0
        }
    }

    /**
     * Calculate available spots for a location
     */
    suspend fun getAvailableSpots(locationId: String, totalSpots: Int): Int {
        val activeBookings = getActiveBookingsCount(locationId)
        val available = maxOf(0, totalSpots - activeBookings)
        android.util.Log.d("BookingManager", "Location $locationId: $available/$totalSpots available")
        return available
    }

    /**
     * Extend a booking by additional hours
     */
    suspend fun extendBooking(bookingId: String, additionalHours: Int): Result<Booking> {
        return try {
            android.util.Log.d("BookingManager", "Extending booking $bookingId by $additionalHours hours")

            // Get current booking using the EXISTING getBooking method
            val currentBooking = getBooking(bookingId)
                ?: return Result.failure(Exception("Booking not found"))

            // Calculate new values
            val newEndTime = currentBooking.endTime + (additionalHours * 60 * 60 * 1000L)
            val additionalCost = currentBooking.pricePerHour * additionalHours
            val newTotalPrice = currentBooking.totalPrice + additionalCost
            val newTotalHours = currentBooking.totalHours + additionalHours

            // Update in Firebase
            bookingsCollection.document(bookingId)
                .update(mapOf(
                    "endTime" to newEndTime,
                    "totalHours" to newTotalHours,
                    "totalPrice" to newTotalPrice
                ))
                .await()

            // Create updated booking object
            val updatedBooking = currentBooking.copy(
                endTime = newEndTime,
                totalHours = newTotalHours,
                totalPrice = newTotalPrice
            )

            android.util.Log.d("BookingManager", "✅ Booking extended successfully!")
            android.util.Log.d("BookingManager", "New end time: ${java.text.SimpleDateFormat("HH:mm").format(newEndTime)}")
            android.util.Log.d("BookingManager", "New total: Rs.$newTotalPrice")

            Result.success(updatedBooking)

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Failed to extend booking: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Confirm a booking (status = "active") using transaction
     * This makes it show as black for everyone
     */
    suspend fun confirmBooking(bookingId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val bookingRef = bookingsCollection.document(bookingId)
                val snapshot = transaction.get(bookingRef)

                // Verify booking exists and is in pending status
                val booking = snapshot.toObject(Booking::class.java)
                    ?: throw Exception("Booking not found")

                if (booking.status != "pending") {
                    throw Exception("Booking is not in pending status (current: ${booking.status})")
                }

                // Check if booking has expired
                if (booking.endTime <= System.currentTimeMillis()) {
                    throw Exception("Booking has expired")
                }

                // Update to active
                transaction.update(bookingRef, "status", "active")
            }.await()

            android.util.Log.d("BookingManager", "Booking confirmed: $bookingId")
            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Confirmation failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel a pending or active booking
     */
    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val bookingRef = bookingsCollection.document(bookingId)
                val snapshot = transaction.get(bookingRef)

                val booking = snapshot.toObject(Booking::class.java)
                    ?: throw Exception("Booking not found")

                if (booking.status == "cancelled" || booking.status == "completed") {
                    throw Exception("Booking already ${booking.status}")
                }

                transaction.update(bookingRef, mapOf(
                    "status" to "cancelled",
                    "cancelledAt" to System.currentTimeMillis()
                ))
            }.await()

            android.util.Log.d("BookingManager", "Booking cancelled: $bookingId")
            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Cancellation failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel a pending booking (backwards compatibility)
     */
    suspend fun cancelPendingBooking(bookingId: String): Result<Unit> {
        return cancelBooking(bookingId)
    }

    /**
     * Complete a booking (mark as finished)
     */
    suspend fun completeBooking(bookingId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId)
                .update(mapOf(
                    "status" to "completed",
                    "completedAt" to System.currentTimeMillis()
                ))
                .await()

            android.util.Log.d("BookingManager", "Booking completed: $bookingId")
            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Completion failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's active bookings (pending or active status)
     */
    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .whereIn("status", listOf("pending", "active"))
                .get()
                .await()

            val currentTime = System.currentTimeMillis()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("BookingManager", "Failed to parse user booking: ${e.message}")
                    null
                }
            }.filter { it.endTime > currentTime }

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Failed to get user bookings: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get user's booking history (all statuses)
     */
    suspend fun getUserBookingHistory(userId: String, limit: Int = 50): List<Booking> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("BookingManager", "Failed to parse booking history: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Failed to get booking history: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Check if a space is currently booked (active only)
     */
    fun isSpaceBooked(spaceId: String): Boolean {
        return bookedSpaces.value.contains(spaceId)
    }

    /**
     * Clean up expired bookings for a location
     * Should be called periodically or when needed
     */
    suspend fun cleanupExpiredBookings(locationId: String): Result<Int> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = bookingsCollection
                .whereEqualTo("locationId", locationId)
                .whereIn("status", listOf("pending", "active"))
                .whereLessThan("endTime", currentTime)
                .get()
                .await()

            var cleanedCount = 0
            snapshot.documents.forEach { doc ->
                try {
                    doc.reference.update(mapOf(
                        "status" to "expired",
                        "expiredAt" to currentTime
                    )).await()
                    cleanedCount++
                } catch (e: Exception) {
                    android.util.Log.e("BookingManager", "Failed to expire booking ${doc.id}: ${e.message}")
                }
            }

            android.util.Log.d("BookingManager", "Cleaned up $cleanedCount expired bookings")
            Result.success(cleanedCount)

        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Cleanup failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get booking by ID
     */
    suspend fun getBooking(bookingId: String): Booking? {
        return try {
            val snapshot = bookingsCollection.document(bookingId).get().await()
            snapshot.toObject(Booking::class.java)
        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Failed to get booking: ${e.message}", e)
            null
        }
    }

    // ✅ DEPRECATED - Use reserveSpace instead (kept for backwards compatibility)
    @Deprecated(
        message = "Use reserveSpace instead for better error handling",
        replaceWith = ReplaceWith("reserveSpace(spaceId, userId, userName, locationId, locationName, spaceLabel, durationHours, pricePerHour)")
    )
    suspend fun bookSpace(
        spaceId: String,
        userId: String,
        userName: String,
        locationId: String,
        locationName: String,
        spaceLabel: String,
        durationHours: Int,
        pricePerHour: Double = 4.12
    ): Result<Booking> {
        return when (val result = reserveSpace(
            spaceId, userId, userName, locationId,
            locationName, spaceLabel, durationHours, pricePerHour
        )) {
            is BookingResult.Success -> Result.success(result.booking)
            is BookingResult.Error -> Result.failure(result.exception ?: Exception(result.message))
        }
    }
}