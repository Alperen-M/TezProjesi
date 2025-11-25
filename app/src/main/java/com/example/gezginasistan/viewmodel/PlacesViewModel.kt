package com.example.gezginasistan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlacesViewModel : ViewModel() {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadNearby(lat: Double, lon: Double) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // API doğrudan PlacesResponse döner
                val response = RetrofitClient.placesApi.getNearbyPlaces(lat, lon)
                _places.value = response.places   // PlacesResponse içindeki places listesi
            } catch (e: Exception) {
                _error.value = "İstek sırasında hata: ${e.message}"
                Log.e("PlacesVM", "Exception: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
