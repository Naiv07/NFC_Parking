package com.example.nfc_parking.data

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

object BookingManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    // Local cache of booked spaces for quick access
    val bookedSpaces = mutableStateOf<Set<String>>(emptySet())

    // Listener for real-time updates
    private var listener: ListenerRegistration? = null

    /**
     * Start listening for real-time booking updates for a specific location
     */
    fun startListening(locationId: String) {
        listener?.remove() // Remove previous listener if exists

        listener = bookingsCollection
            .whereEqualTo("locationId", locationId)
            .whereIn("status", listOf("active"))  // ✅ Only show CONFIRMED bookings as black
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BookingManager", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val activeBookings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)
                    }.filter { booking ->
                        // Only include bookings that haven't expired
                        booking.endTime > currentTime
                    }

                    // Update local cache - only ACTIVE bookings show as black
                    val newBookedSpaces = activeBookings.map { it.spaceId }.toSet()
                    android.util.Log.d("BookingManager", "Updated booked spaces: $newBookedSpaces")
                    bookedSpaces.value = newBookedSpaces
                }
            }
    }

    /**
     * Stop listening for updates
     */
    fun stopListening() {
        listener?.remove()
        listener = null
    }

    /**
     * Reserve a parking space (status = "pending")
     * This doesn't show as black yet - just prevents double booking
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
    ): Result<Booking> {
        return try {
            // Check if space is already booked OR reserved
            val existingBooking = bookingsCollection
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("locationId", locationId)
                .whereIn("status", listOf("pending", "active"))
                .get()
                .await()

            if (!existingBooking.isEmpty) {
                val booking = existingBooking.documents.first().toObject(Booking::class.java)
                if (booking != null && booking.endTime > System.currentTimeMillis()) {
                    return Result.failure(Exception("Space already booked"))
                }
            }

            // Create new PENDING booking
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationHours * 60 * 60 * 1000L)
            val bookingId = bookingsCollection.document().id

            val booking = Booking(
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
                status = "pending"  // ✅ Pending - won't show as black yet
            )

            // Save to Firestore
            bookingsCollection.document(bookingId).set(booking).await()

            android.util.Log.d("BookingManager", "Space reserved (pending): ${booking.spaceId}")

            Result.success(booking)
        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Reservation failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Confirm a booking (status = "active")
     * This makes it show as black for everyone
     */
    suspend fun confirmBooking(bookingId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId)
                .update("status", "active")
                .await()

            android.util.Log.d("BookingManager", "Booking confirmed: $bookingId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Confirmation failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Cancel a pending booking (if user backs out before confirming)
     */
    suspend fun cancelPendingBooking(bookingId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId)
                .update("status", "cancelled")
                .await()

            android.util.Log.d("BookingManager", "Booking cancelled: $bookingId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("BookingManager", "Cancellation failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get user's active bookings
     */
    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .whereIn("status", listOf("pending", "active"))
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)
            }.filter { it.endTime > System.currentTimeMillis() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a space is currently booked (active only)
     */
    fun isSpaceBooked(spaceId: String): Boolean {
        return bookedSpaces.value.contains(spaceId)
    }

    // ✅ DEPRECATED - Use reserveSpace instead
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
        return reserveSpace(
            spaceId, userId, userName, locationId,
            locationName, spaceLabel, durationHours, pricePerHour
        )
    }
}