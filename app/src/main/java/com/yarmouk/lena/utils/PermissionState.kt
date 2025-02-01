package com.yarmouk.lena.utils

/**
 * PermissionState.kt
 *
 * This Kotlin file defines a composable function and a data class to manage and track the state of permissions in the LENA application.
 * It leverages Jetpack Compose and Activity Result APIs to handle permission requests and responses.
 *
 * Key Components:
 * - `PermissionState` Data Class:
 *   - `permissionsGranted`: A boolean indicating whether all required permissions are granted.
 *   - `showRationale`: A boolean indicating whether the app should show a rationale for requesting permissions.
 *   - `permissionLauncher`: An `ActivityResultLauncher` to launch permission requests and handle their results.
 *
 * - `rememberPermissionState` Composable Function:
 *   - This function creates and remembers the state required to manage permissions.
 *   - It uses `LocalContext` to access the current context.
 *   - Initializes `permissionsGranted` using `PermissionManager.checkPermissions` to check if all permissions are granted.
 *   - Initializes `showRationale` to determine if a rationale should be shown for requesting permissions.
 *   - Sets up `permissionLauncher` using `rememberLauncherForActivityResult` with a contract to request multiple permissions and a callback to handle the results.
 *   - Returns a `PermissionState` object containing the current permission state and the launcher for requesting permissions.
 *
 * Usage:
 * - The `rememberPermissionState` function can be used within a composable to manage permission requests and handle their results.
 * - It provides an easy way to check the current permission status and launch permission requests when needed.
 *
 * This utility ensures that the LENA application efficiently handles permission requests and maintains a responsive user interface using Jetpack Compose.
 */

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class PermissionState(
    val permissionsGranted: Boolean,
    val showRationale: Boolean,
    val permissionLauncher: ActivityResultLauncher<Array<String>>
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun rememberPermissionState(
    onPermissionsResult: (Map<String, Boolean>) -> Unit
): PermissionState {
    val context = LocalContext.current
    val permissionsGranted = remember {
        mutableStateOf(PermissionManager.checkPermissions(context).all { it.value })
    }

    val showRationale = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onPermissionsResult
    )

    return PermissionState(
        permissionsGranted = permissionsGranted.value,
        showRationale = showRationale.value,
        permissionLauncher = permissionLauncher
    )
}