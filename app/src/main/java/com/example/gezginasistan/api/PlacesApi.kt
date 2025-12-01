package com.example.gezginasistan.api

import com.example.gezginasistan.model.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {

    // Yakındaki mekanları getir
    @GET("api/v1/places/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = 1500,       // Varsayılan 1500 metre
        @Query("type") type: String? = null        // Varsayılan null → backend default kullanır
    ): PlacesResponse
}
