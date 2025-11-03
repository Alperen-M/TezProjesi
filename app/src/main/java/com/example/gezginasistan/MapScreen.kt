package com.example.gezginasistan

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen() {
    val istanbul = LatLng(41.0082, 28.9784)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istanbul, 10f)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState
    ) {
        // ✅ Marker artık state ile tanımlanıyor
        Marker(
            state = rememberMarkerState(position = istanbul),
            title = "İstanbul"
        )
    }
}
