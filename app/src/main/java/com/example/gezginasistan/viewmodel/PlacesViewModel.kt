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

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Hata oluşursa çökmemesi için lazy veya init bloğunda kontrol edilebilir ama şimdilik kalsın
    private val placesApi: PlacesApi = RetrofitClient.create<PlacesApi>()
    private val gezginApi: GezginAsistanApi = RetrofitClient.create<GezginAsistanApi>()

    fun loadNearby(lat: Double, lon: Double) {
        _loading.value = true
        _error.value = null

        Log.d("PlacesVM", "İstek atılıyor: Lat:$lat Lon:$lon") // ✅ LOG 1

        viewModelScope.launch {
            try {
                val response = placesApi.getNearbyPlaces(lat, lon)

                // ✅ LOG 2: Gelen ham veriyi kontrol et
                Log.d("PlacesVM", "API Yanıtı Geldi. Liste Boyutu: ${response.places?.size}")

                if (response.places.isNullOrEmpty()) {
                    Log.w("PlacesVM", "UYARI: Gelen liste BOŞ! Modelde SerializedName hatası olabilir.")
                }

                _places.value = response.places ?: emptyList()

            } catch (e: Exception) {
                _error.value = "Hata: ${e.localizedMessage}"
                Log.e("PlacesVM", "API ÇAĞRISI HATASI", e) // ✅ LOG 3: Hata detayını gör
            } finally {
                _loading.value = false
            }
        }
    }

    // ... diğer kodların aynı kalsın ...
    private val _recommendations = MutableStateFlow<List<PlaceResponseData>>(emptyList())
    val recommendations: StateFlow<List<PlaceResponseData>> = _recommendations

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations

    fun fetchRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            try {
                val result = gezginApi.getRecommendationsNearby()
                Log.d("PlacesVM", "Öneriler geldi: ${result.size} adet")
                _recommendations.value = result
            } catch (e: Exception) {
                Log.e("PlacesVM", "Öneri Hatası", e)
                _recommendations.value = emptyList()
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }
}