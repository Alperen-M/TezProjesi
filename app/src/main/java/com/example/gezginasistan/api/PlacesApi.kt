package com.example.gezginasistan.api

import com.example.gezginasistan.model.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {

    @GET("api/v1/places/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = 1500,
        @Query("type") type: String = "restaurant"
    ): PlacesResponse
}
