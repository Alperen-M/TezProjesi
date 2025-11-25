package com.example.gezginasistan.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.gezginasistan.viewmodel.MapViewModel
import com.example.gezginasistan.model.Place

@Composable
fun MapScreen(
    userLatitude: Double = 38.619,
    userLongitude: Double = 27.428
) {
    val viewModel: MapViewModel = viewModel()
    val nearbyPlaces = viewModel.nearbyPlaces.value ?: emptyList()

    val initialLocation = LatLng(userLatitude, userLongitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchNearbyPlaces(userLatitude, userLongitude)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        nearbyPlaces.forEach { place: Place ->
            Marker(
                state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                title = place.name,
                snippet = "ID: ${place.id}"
            )
        }
    }
}
