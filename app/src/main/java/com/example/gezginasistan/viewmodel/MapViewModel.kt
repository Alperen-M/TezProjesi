package com.example.gezginasistan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.GezginAsistanApi
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.VisitRequestData
import com.example.gezginasistan.model.VisitResponseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VisitUiState {
    object Idle : VisitUiState()
    object Loading : VisitUiState()
    data class Success(val data: VisitResponseData) : VisitUiState()
    data class Error(val message: String) : VisitUiState()
}

class MapViewModel : ViewModel() {

    private val api: GezginAsistanApi = RetrofitClient.create<GezginAsistanApi>()

    private val _visitState = MutableStateFlow<VisitUiState>(VisitUiState.Idle)
    val visitState: StateFlow<VisitUiState> = _visitState

    fun recordVisit(placeId: String, placeName: String, category: String) {
        viewModelScope.launch {
            _visitState.value = VisitUiState.Loading
            try {
                val request = VisitRequestData(
                    place_id = placeId,
                    place_name = placeName,
                    place_category = category
                )
                Log.d("MapVM", "recordVisit çağrıldı: $request")

                val response = api.recordVisit(request)
                Log.d("MapVM", "recordVisit yanıt: ${response.code()} ${response.message()}")

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Log.d("MapVM", "Visit Success: $body")
                        _visitState.value = VisitUiState.Success(body)
                    } ?: run {
                        Log.e("MapVM", "Boş yanıt gövdesi")
                        _visitState.value = VisitUiState.Error("Boş yanıt gövdesi")
                    }
                } else {
                    Log.e("MapVM", "Visit failed: ${response.code()} ${response.message()}")
                    _visitState.value = VisitUiState.Error(
                        "Visit failed: ${response.code()} ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("MapVM", "recordVisit hata", e)
                _visitState.value = VisitUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
