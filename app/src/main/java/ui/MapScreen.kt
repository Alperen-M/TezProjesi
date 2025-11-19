package com.example.gezginasistan.ui

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen() {
    val istanbul = LatLng(41.0082, 28.9784) // İstanbul koordinatları
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istanbul, 10f)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState
    )
}
