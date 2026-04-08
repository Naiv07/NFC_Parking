package com.example.nfc_parking.data

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    // Permission request codes
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    const val CAMERA_PERMISSION_REQUEST_CODE = 1002
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
    const val ALL_PERMISSIONS_REQUEST_CODE = 1000

    // Required permissions
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA
    )

    // Notification permission (Android 13+)
    private val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    /**
     * Get all required permissions based on Android version
     */
    fun getAllRequiredPermissions(): Array<String> {
        return locationPermissions + cameraPermissions + notificationPermissions
    }

    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all location permissions are granted
     */
    fun areLocationPermissionsGranted(context: Context): Boolean {
        return locationPermissions.all { permission ->
            isPermissionGranted(context, permission)
        }
    }

    /**
     * Check if camera permission is granted
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Not required for older versions
        }
    }

    /**
     * Check if ALL required permissions are granted
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        return areLocationPermissionsGranted(context) &&
                isCameraPermissionGranted(context) &&
                isNotificationPermissionGranted(context)
    }

    /**
     * Request all permissions at once
     */
    fun requestAllPermissions(activity: Activity) {
        val allPermissions = getAllRequiredPermissions()
        ActivityCompat.requestPermissions(
            activity,
            allPermissions,
            ALL_PERMISSIONS_REQUEST_CODE
        )
    }

    /**
     * Request location permissions
     */
    fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            locationPermissions,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Request camera permission
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            cameraPermissions,
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                notificationPermissions,
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Check if user has permanently denied a permission
     */
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Get list of denied permissions
     */
    fun getDeniedPermissions(context: Context): List<String> {
        return getAllRequiredPermissions().filter { permission ->
            !isPermissionGranted(context, permission)
        }
    }

    /**
     * Handle permission request result
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onAllGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        when (requestCode) {
            ALL_PERMISSIONS_REQUEST_CODE,
            LOCATION_PERMISSION_REQUEST_CODE,
            CAMERA_PERMISSION_REQUEST_CODE,
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                val deniedPermissions = mutableListOf<String>()

                permissions.forEachIndexed { index, permission ->
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permission)
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    onAllGranted()
                } else {
                    onDenied(deniedPermissions)
                }
            }
        }
    }
}