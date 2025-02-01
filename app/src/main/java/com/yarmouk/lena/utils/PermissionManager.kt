package com.yarmouk.lena.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionManager.kt
 *
 * This Kotlin object, `PermissionManager`, is a utility class designed to handle permission-related tasks for the LENA application.
 * It provides a centralized way to manage, request, and track the status of various permissions required by the app, specifically for devices running Android S (API level 31) and above.
 *
 * Key Components:
 * - Constants:
 *   - `PREFS_NAME`: Name of the shared preferences file used to store permission-related data.
 *   - `KEY_PERMISSIONS_REQUESTED`: Key used to track if permissions have been requested before.
 *   - `REQUIRED_PERMISSIONS`: A list of permissions required by the app, including location, audio recording, calendar access, and Bluetooth connectivity.
 *
 * - Permission Checks:
 *   - `checkPermissions(context: Context)`: Returns a map of permissions and their grant status.
 *   - `areAllPermissionsGranted(context: Context)`: Checks if all required permissions are granted.
 *   - `getUngrantedPermissions(context: Context)`: Returns a list of permissions that are not granted.
 *
 * - Rationale Display:
 *   - `shouldShowRationale(activity: Activity, permissions: List<String>)`: Determines if the app should show a rationale for requesting permissions.
 *
 * - Settings Navigation:
 *   - `openAppSettings(context: Context)`: Opens the app settings page to allow users to manually grant permissions.
 *
 * - Shared Preferences Management:
 *   - `updatePermissionsRequested(context: Context)`: Updates the shared preferences to indicate that permissions have been requested.
 *   - `hasRequestedPermissionsBefore(context: Context)`: Checks if permissions have been requested before.
 *   - `resetPermissionTracking(context: Context)`: Resets the permission tracking in shared preferences.
 *
 * The `PermissionManager` object ensures that the necessary permissions are managed efficiently and provides utility functions to handle permission requests, status checks, and user prompts.
 */

object PermissionManager {
    const val PREFS_NAME = "app_preferences"
    const val KEY_PERMISSIONS_REQUESTED = "permissions_requested"

    @RequiresApi(Build.VERSION_CODES.S)
    val REQUIRED_PERMISSIONS = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkPermissions(context: Context): Map<String, Boolean> {
        return REQUIRED_PERMISSIONS.associateWith { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun shouldShowRationale(activity: Activity, permissions: List<String>): Boolean {
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    fun openAppSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(this)
        }
    }

    fun updatePermissionsRequested(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSIONS_REQUESTED, true)
            .apply()
    }

    fun hasRequestedPermissionsBefore(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PERMISSIONS_REQUESTED, false)
    }
}