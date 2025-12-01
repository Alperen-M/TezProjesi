package com.example.gezginasistan.model

// ✅ Backend PlaceResponse şemasına uygun data class
data class PlaceResponseData(
    val place_id: String,
    val place_name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    val ai_score: Double
)
