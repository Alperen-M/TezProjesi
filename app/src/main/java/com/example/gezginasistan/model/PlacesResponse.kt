package com.example.gezginasistan.model

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    // JSON'daki "results" anahtarını bizim "places" değişkenine bağlıyoruz
    @SerializedName("results")
    val places: List<Place>? = emptyList(),

    val status: String? = null
)