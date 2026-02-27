package com.example.nfc_parking.data

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import com.example.nfc_parking.receivers.ParkingAlertBroadcastReceiver
import kotlinx.coroutines.*
import java.util.*

// Alert data class
data class Alert(
    val id: String,
    val title: String,
    val message: String,
    val location: String,
    val spotNumber: String,
    val timeRemaining: String,
    val endTime: Long,
    val urgency: AlertUrgency,
    val timestamp: Long = System.currentTimeMillis(),
    val bookingId: String? = null,
    val amount: Double? = null
)

enum class AlertUrgency {
    HIGH, MEDIUM, LOW
}

object AlertsManager {
    // Observable list of alerts that triggers recomposition
    private val _alerts = mutableStateListOf<Alert>()
    val alerts: List<Alert> get() = _alerts.toList()

    // Keep track of active monitoring jobs
    private val monitoringJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Context for sending broadcasts
    private var appContext: Context? = null

    /**
     * Initialize with application context
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Add a new alert
     */
    fun addAlert(alert: Alert) {
        // Avoid duplicates
        if (_alerts.none { it.id == alert.id }) {
            _alerts.add(0, alert) // Add to top of list
        }
    }

    /**
     * Remove an alert by ID
     */
    fun dismissAlert(alertId: String) {
        _alerts.removeAll { it.id == alertId }
    }

    /**
     * Remove an alert by ID (alias for dismissAlert)
     */
    fun removeAlert(alertId: String) {
        dismissAlert(alertId)
    }

    /**
     * Clear all alerts
     */
    fun clearAllAlerts() {
        _alerts.clear()
    }

    /**
     * Clear all alerts (alias for clearAllAlerts)
     */
    fun clearAll() {
        clearAllAlerts()
    }

    /**
     * Show booking confirmation alert + Send Broadcast
     */
    fun showBookingConfirmation(booking: Booking) {
        val alert = Alert(
            id = "booking_${booking.bookingId}_${System.currentTimeMillis()}",
            title = "Booking Confirmed",
            message = "Your parking has been successfully booked!",
            location = booking.locationName,
            spotNumber = booking.spaceLabel,
            timeRemaining = "${booking.totalHours} ${if (booking.totalHours == 1) "hour" else "hours"}",
            endTime = booking.endTime,
            urgency = AlertUrgency.LOW,
            bookingId = booking.bookingId,
            amount = booking.totalPrice
        )
        addAlert(alert)

        // 📡 SEND BROADCAST
        sendBroadcast(
            ParkingAlertBroadcastReceiver.ACTION_BOOKING_CONFIRMED,
            booking.locationName,
            booking.spaceLabel,
            "",
            booking.totalPrice.toString(),
            booking.bookingId,
            "LOW"
        )

        // Start monitoring this booking for time warnings
        startMonitoringBooking(booking)
    }

    /**
     * Show cancellation and refund alert + Send Broadcast
     */
    fun showCancellationRefund(booking: Booking) {
        val alert = Alert(
            id = "refund_${booking.bookingId}_${System.currentTimeMillis()}",
            title = "Booking Cancelled - Refund Processed",
            message = "Your booking has been cancelled. Amount of Rs.${String.format("%.2f", booking.totalPrice)} has been refunded.",
            location = booking.locationName,
            spotNumber = booking.spaceLabel,
            timeRemaining = "Cancelled",
            endTime = System.currentTimeMillis(),
            urgency = AlertUrgency.MEDIUM,
            bookingId = booking.bookingId,
            amount = booking.totalPrice
        )
        addAlert(alert)

        // 📡 SEND BROADCAST
        sendBroadcast(
            ParkingAlertBroadcastReceiver.ACTION_BOOKING_CANCELLED,
            booking.locationName,
            booking.spaceLabel,
            "",
            booking.totalPrice.toString(),
            booking.bookingId,
            "MEDIUM"
        )

        // Stop monitoring this booking
        stopMonitoringBooking(booking.bookingId)
    }

    /**
     * Start monitoring a booking for time-based alerts
     */
    private fun startMonitoringBooking(booking: Booking) {
        // Cancel any existing monitoring job for this booking
        stopMonitoringBooking(booking.bookingId)

        val job = scope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val timeRemaining = booking.endTime - currentTime

                when {
                    // Parking expired
                    timeRemaining <= 0 -> {
                        showParkingExpired(booking)
                        cancel() // Stop monitoring
                    }
                    // 15 minutes warning (HIGH urgency)
                    timeRemaining <= 15 * 60 * 1000 && timeRemaining > 14 * 60 * 1000 -> {
                        showParkingExpiringSoon(booking, timeRemaining, AlertUrgency.HIGH)
                    }
                    // 30 minutes warning (MEDIUM urgency)
                    timeRemaining <= 30 * 60 * 1000 && timeRemaining > 29 * 60 * 1000 -> {
                        showParkingExpiringSoon(booking, timeRemaining, AlertUrgency.MEDIUM)
                    }
                    // 1 hour warning (LOW urgency)
                    timeRemaining <= 60 * 60 * 1000 && timeRemaining > 59 * 60 * 1000 -> {
                        showParkingExpiringSoon(booking, timeRemaining, AlertUrgency.LOW)
                    }
                }

                // Check every minute
                delay(60 * 1000)
            }
        }

        monitoringJobs[booking.bookingId] = job
    }

    /**
     * Stop monitoring a booking
     */
    private fun stopMonitoringBooking(bookingId: String) {
        monitoringJobs[bookingId]?.cancel()
        monitoringJobs.remove(bookingId)
    }

    /**
     * Show parking expiring soon alert + Send Broadcast
     */
    private fun showParkingExpiringSoon(booking: Booking, timeRemaining: Long, urgency: AlertUrgency) {
        val minutes = (timeRemaining / (60 * 1000)).toInt()

        // Remove any previous "expiring soon" alerts for this booking
        _alerts.removeAll { it.bookingId == booking.bookingId && it.title.contains("Expiring Soon") }

        val alert = Alert(
            id = "expiring_${booking.bookingId}_${urgency.name}",
            title = "Parking Time Expiring Soon",
            message = "Your parking at ${booking.locationName} expires in $minutes minutes",
            location = booking.locationName,
            spotNumber = booking.spaceLabel,
            timeRemaining = "$minutes min",
            endTime = booking.endTime,
            urgency = urgency,
            bookingId = booking.bookingId,
            amount = booking.totalPrice
        )
        addAlert(alert)

        // 📡 SEND BROADCAST
        sendBroadcast(
            ParkingAlertBroadcastReceiver.ACTION_PARKING_EXPIRING_SOON,
            booking.locationName,
            booking.spaceLabel,
            "$minutes min",
            booking.totalPrice.toString(),
            booking.bookingId,
            urgency.name
        )
    }

    /**
     * Show parking expired alert + Send Broadcast
     */
    private fun showParkingExpired(booking: Booking) {
        // Remove any previous alerts for this booking
        _alerts.removeAll { it.bookingId == booking.bookingId }

        val alert = Alert(
            id = "expired_${booking.bookingId}_${System.currentTimeMillis()}",
            title = "Parking Time Expired",
            message = "Your parking time at ${booking.locationName} has expired",
            location = booking.locationName,
            spotNumber = booking.spaceLabel,
            timeRemaining = "Expired",
            endTime = booking.endTime,
            urgency = AlertUrgency.HIGH,
            bookingId = booking.bookingId,
            amount = booking.totalPrice
        )
        addAlert(alert)

        // 📡 SEND BROADCAST
        sendBroadcast(
            ParkingAlertBroadcastReceiver.ACTION_PARKING_EXPIRED,
            booking.locationName,
            booking.spaceLabel,
            "Expired",
            booking.totalPrice.toString(),
            booking.bookingId,
            "HIGH"
        )
    }

    /**
     * Send Broadcast to ParkingAlertBroadcastReceiver
     */
    private fun sendBroadcast(
        action: String,
        locationName: String,
        spaceLabel: String,
        timeRemaining: String,
        amount: String,
        bookingId: String,
        urgency: String
    ) {
        appContext?.let { context ->
            val intent = Intent(context, ParkingAlertBroadcastReceiver::class.java).apply {
                this.action = action
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_LOCATION_NAME, locationName)
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_SPACE_LABEL, spaceLabel)
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_TIME_REMAINING, timeRemaining)
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_AMOUNT, amount)
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_BOOKING_ID, bookingId)
                putExtra(ParkingAlertBroadcastReceiver.EXTRA_URGENCY, urgency)
            }
            context.sendBroadcast(intent)
        }
    }

    /**
     * Initialize monitoring for all active bookings
     */
    fun initializeMonitoring() {
        val activeBookings = BookingHistory.history.filter { it.status == BookingStatus.ACTIVE }
        activeBookings.forEach { historyItem ->
            startMonitoringBooking(historyItem.booking)
        }
    }

    /**
     * Clean up when app is destroyed
     */
    fun cleanup() {
        monitoringJobs.values.forEach { it.cancel() }
        monitoringJobs.clear()
        scope.cancel()
    }

    /**
     * Get unread alert count (for badge)
     */
    fun getUnreadCount(): Int {
        return _alerts.size
    }
}