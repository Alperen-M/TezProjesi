package com.example.gezginasistan.model

data class Place(
    val id: Int,                     // Backend'deki benzersiz ID
    val name: String,                // Mekan adı
    val latitude: Double,            // Enlem
    val longitude: Double,           // Boylam
    val category: String? = null,    // Kategori (ör. restoran, kafe, park)
    val address: String? = null,     // Adres bilgisi
    val distance: Double? = null,    // Kullanıcıya olan mesafe (metre)
    val place_id: String? = null,    // Harici sistemlerde kullanılan ID (opsiyonel)
    val rating: Double? = null,      // Ortalama puan (opsiyonel)
    val open_now: Boolean? = null    // Şu anda açık mı? (opsiyonel)
)
