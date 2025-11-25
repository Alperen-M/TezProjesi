package com.example.gezginasistan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.Place
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class MapViewModel : ViewModel() {

    private val _nearbyPlaces = mutableStateOf<List<Place>>(emptyList())
    val nearbyPlaces: State<List<Place>> = _nearbyPlaces

    fun fetchNearbyPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                // placesApi doğrudan PlacesResponse döner
                val response = RetrofitClient.placesApi.getNearbyPlaces(latitude, longitude)
                _nearbyPlaces.value = response.places   // PlacesResponse içindeki places listesi
            } catch (e: Exception) {
                e.printStackTrace()
                _nearbyPlaces.value = emptyList()
            }
        }
    }
}
