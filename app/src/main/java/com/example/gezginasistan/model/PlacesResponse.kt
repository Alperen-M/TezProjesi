package com.example.gezginasistan.model

// Ana yanıt objesi
data class PlacesResponse(
    val places: List<Place>,        // Mekan listesi
    val total: Int? = null,         // Toplam sonuç sayısı (opsiyonel)
    val status: String? = null,     // Backend'in döndüğü durum bilgisi (opsiyonel)
    val error: String? = null       // Hata mesajı (opsiyonel)
)
