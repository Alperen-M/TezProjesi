package com.example.gezginasistan.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)

@Composable
fun LocationPermissionRequest(
    onPermissionGranted: @Composable () -> Unit
) {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    when {
        permissionState.status.isGranted -> {
            onPermissionGranted() // ✅ Artık burada Composable çağırabilirsin
        }
        permissionState.status.shouldShowRationale -> {
            Text("Konum izni gerekli, lütfen ayarlardan verin.")
        }
        else -> {
            Text("Konum izni reddedildi ❌")
        }
    }
}