package com.example.gezginasistan.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.gezginasistan.viewmodel.MapViewModel
import com.example.gezginasistan.viewmodel.VisitUiState
import com.example.gezginasistan.viewmodel.PlacesViewModel
import com.example.gezginasistan.model.Place
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel = viewModel(),
    placesViewModel: PlacesViewModel = viewModel(),
    selectedPlaceId: String? = null,
    selectedLat: Double? = null,
    selectedLon: Double? = null
) {
    val visitState = mapViewModel.visitState.collectAsState()
    val placesState = placesViewModel.places.collectAsState()
    val scope = rememberCoroutineScope()

    // Kullanıcının haritada tıkladığı konumu tutan değişken
    var clickedLocation by remember { mutableStateOf<LatLng?>(null) }

    val defaultPosition = LatLng(38.5, 27.7)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 12f)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("MapScreen", "Yakındaki mekanlar yükleniyor...")
        placesViewModel.loadNearby(defaultPosition.latitude, defaultPosition.longitude)
    }

    LaunchedEffect(selectedLat, selectedLon) {
        if (selectedLat != null && selectedLon != null) {
            Log.d("MapScreen", "Kamerayı öneri noktasına odakla: $selectedLat,$selectedLon")
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(selectedLat, selectedLon),
                15f
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ Harita") },
                actions = {
                    TextButton(onClick = { navController.navigate("recommendations") }) {
                        Text("Öneriler", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    tiltGesturesEnabled = true
                ),
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    isTrafficEnabled = false
                ),
                onMapClick = { latLng ->
                    Log.d("MapScreen", "Haritaya tıklandı: ${latLng.latitude}, ${latLng.longitude}")
                    clickedLocation = latLng
                }
            ) {
                // --- Backend'den gelen mekanlar (Kırmızı Pinler) ---
                placesState.value.forEach { place: Place ->
                    // DÜZELTME: place.id yerine place.place_id kullanıyoruz.
                    // Eğer place_id null gelirse hata vermemesi için varsayılan değer atıyoruz.
                    val safeId = place.place_id ?: "unknown_id_${place.hashCode()}"

                    val markerState = rememberMarkerState(
                        key = safeId,
                        position = LatLng(place.latitude, place.longitude)
                    )

                    Marker(
                        state = markerState,
                        title = place.name.ifBlank { "Bilinmeyen Mekan" },
                        snippet = (place.category) + " • " + (place.address ?: "Adres yok"),
                        onClick = {
                            Log.d("MapScreen", "Mekan Marker tıklandı: ${place.name}")
                            mapViewModel.recordVisit(
                                placeId = safeId,
                                placeName = place.name,
                                category = place.category
                            )
                            false
                        }
                    )
                }

                // --- Önerilerden gelen seçili mekan ---
                if (selectedLat != null && selectedLon != null) {
                    val selState = rememberMarkerState(
                        key = selectedPlaceId ?: "selected",
                        position = LatLng(selectedLat, selectedLon)
                    )
                    Marker(
                        state = selState,
                        title = selectedPlaceId ?: "Seçili Mekan",
                        snippet = "Önerilerden seçildi",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            Log.d("MapScreen", "Öneri marker tıklandı")
                            false
                        }
                    )
                }

                // --- Kullanıcının elle tıkladığı yere koyulan Marker (Mavi) ---
                clickedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Seçilen Konum",
                        snippet = "${location.latitude}, ${location.longitude}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        onClick = {
                            Log.d("MapScreen", "Elle koyulan pine tıklandı")
                            false
                        }
                    )
                }
            }

            // "Burada Ara" Butonu
            clickedLocation?.let { loc ->
                Button(
                    onClick = {
                        Log.d("MapScreen", "Yeni konumda arama yapılıyor: ${loc.latitude}, ${loc.longitude}")
                        placesViewModel.loadNearby(loc.latitude, loc.longitude)

                        scope.launch {
                            snackbarHostState.showSnackbar("Bu konumdaki mekanlar getiriliyor...")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🔎 Burada Öneri Ara")
                }
            }
        }
    }

    // VisitUiState kontrolü
    when (val state = visitState.value) {
        VisitUiState.Idle -> Unit
        VisitUiState.Loading -> {
            LaunchedEffect("loading") {
                Log.d("MapScreen", "Visit Loading")
                snackbarHostState.showSnackbar("Ziyaret kaydediliyor…")
            }
        }
        is VisitUiState.Success -> {
            LaunchedEffect(state) {
                Log.d("MapScreen", "Visit Success: ${state.data}")
                snackbarHostState.showSnackbar("Ziyaret kaydedildi: ${state.data.place_name}")
            }
        }
        is VisitUiState.Error -> {
            LaunchedEffect(state) {
                Log.e("MapScreen", "Visit Error: ${state.message}")
                snackbarHostState.showSnackbar("Hata: ${state.message}")
            }
        }
    }
}