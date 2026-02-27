package com.example.nfc_parking.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nfc_parking.MainActivity
import com.example.nfc_parking.R

/**
 * Custom Broadcast Receiver for Parking Alerts
 * Handles different types of parking notifications
 */
class ParkingAlertBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "parking_alerts_channel"
        const val CHANNEL_NAME = "Parking Alerts"

        // Alert Types
        const val ACTION_BOOKING_CONFIRMED = "com.example.nfc_parking.BOOKING_CONFIRMED"
        const val ACTION_PARKING_EXPIRING_SOON = "com.example.nfc_parking.PARKING_EXPIRING_SOON"
        const val ACTION_PARKING_EXPIRED = "com.example.nfc_parking.PARKING_EXPIRED"
        const val ACTION_BOOKING_CANCELLED = "com.example.nfc_parking.BOOKING_CANCELLED"

        // Extra Keys
        const val EXTRA_LOCATION_NAME = "location_name"
        const val EXTRA_SPACE_LABEL = "space_label"
        const val EXTRA_TIME_REMAINING = "time_remaining"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_BOOKING_ID = "booking_id"
        const val EXTRA_URGENCY = "urgency"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Create notification channel (Android 8.0+)
        createNotificationChannel(context)

        when (intent.action) {
            ACTION_BOOKING_CONFIRMED -> handleBookingConfirmed(context, intent)
            ACTION_PARKING_EXPIRING_SOON -> handleParkingExpiringSoon(context, intent)
            ACTION_PARKING_EXPIRED -> handleParkingExpired(context, intent)
            ACTION_BOOKING_CANCELLED -> handleBookingCancelled(context, intent)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for parking bookings and alerts"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleBookingConfirmed(context: Context, intent: Intent) {
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Parking"
        val spaceLabel = intent.getStringExtra(EXTRA_SPACE_LABEL) ?: ""
        val amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Booking Confirmed!")
            .setContentText("$locationName - Space $spaceLabel | ₹$amount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your parking has been successfully booked at $locationName. Space: $spaceLabel. Amount: ₹$amount"))
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleParkingExpiringSoon(context: Context, intent: Intent) {
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Parking"
        val timeRemaining = intent.getStringExtra(EXTRA_TIME_REMAINING) ?: ""
        val urgency = intent.getStringExtra(EXTRA_URGENCY) ?: "MEDIUM"

        val icon = when (urgency) {
            "HIGH" -> "🔴"
            "MEDIUM" -> "🟡"
            else -> "🟢"
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$icon Parking Time Expiring Soon")
            .setContentText("Your parking at $locationName expires in $timeRemaining")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleParkingExpired(context: Context, intent: Intent) {
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Parking"
        val spaceLabel = intent.getStringExtra(EXTRA_SPACE_LABEL) ?: ""

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Parking Time Expired")
            .setContentText("Your parking time at $locationName has expired")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleBookingCancelled(context: Context, intent: Intent) {
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Parking"
        val amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💰 Refund Processed")
            .setContentText("Booking cancelled. ₹$amount refunded to your account")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }
}