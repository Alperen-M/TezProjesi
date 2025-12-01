package com.example.gezginasistan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.PlacesApi
import com.example.gezginasistan.api.GezginAsistanApi
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.Place
import com.example.gezginasistan.model.PlaceResponseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlacesViewModel : ViewModel() {

    // ✅ Boş liste ile başlat -> null olmayacak
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val placesApi: PlacesApi = RetrofitClient.create<PlacesApi>()
    private val gezginApi: GezginAsistanApi = RetrofitClient.create<GezginAsistanApi>()

    fun loadNearby(lat: Double, lon: Double) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = placesApi.getNearbyPlaces(lat, lon)
                // ✅ Null kontrolü
                _places.value = response.places ?: emptyList()
            } catch (e: Exception) {
                _error.value = "İstek sırasında hata: ${e.message}"
                Log.e("PlacesVM", "Exception: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // 🔹 Hafta 5: Öneri sistemi için yeni state
    private val _recommendations = MutableStateFlow<List<PlaceResponseData>>(emptyList())
    val recommendations: StateFlow<List<PlaceResponseData>> = _recommendations

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations

    fun fetchRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            try {
                // ✅ Doğru endpoint: /recommendations/nearby
                val result = gezginApi.getRecommendationsNearby()
                _recommendations.value = result
            } catch (e: Exception) {
                Log.e("PlacesVM", "Öneri isteğinde hata: ${e.message}", e)
                _recommendations.value = emptyList()
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }
}
