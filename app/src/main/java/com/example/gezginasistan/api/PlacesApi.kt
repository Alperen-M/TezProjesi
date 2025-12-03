package com.example.gezginasistan.api

import com.example.gezginasistan.model.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {
    @GET("api/v1/places/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int = 1500
    ): PlacesResponse // Burası önemli
}
