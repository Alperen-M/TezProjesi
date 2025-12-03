package com.example.gezginasistan.model

import com.google.gson.annotations.SerializedName

data class Place(
    val place_id: String? = null,
    val name: String = "",

    // Google adresi 'vicinity' olarak gönderiyor
    @SerializedName("vicinity")
    val address: String? = null,

    // Kategori 'types' dizisinin içinde
    val types: List<String>? = null,

    // Koordinatlar en dışta değil, 'geometry' kutusunun içinde!
    val geometry: Geometry? = null
) {
    // UI tarafında kolay kullanım için yardımcılar
    val latitude: Double
        get() = geometry?.location?.lat ?: 0.0

    val longitude: Double
        get() = geometry?.location?.lng ?: 0.0

    val category: String
        get() = types?.firstOrNull() ?: "Genel"
}

// Helper Class'lar (İç içe kutuları açmak için)
data class Geometry(val location: LocationData? = null)
data class LocationData(val lat: Double = 0.0, val lng: Double = 0.0)