package com.example.lena.utils

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