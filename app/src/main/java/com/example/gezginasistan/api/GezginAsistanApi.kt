package com.example.gezginasistan.api

import com.example.gezginasistan.model.VisitRequestData
import com.example.gezginasistan.model.VisitResponseData
import com.example.gezginasistan.model.PlaceResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GezginAsistanApi {

    @POST("places/visit")
    suspend fun recordVisit(
        @Body request: VisitRequestData
    ): Response<VisitResponseData>

    // ✅ Doğru endpoint: /recommendations/nearby
    @GET("recommendations/nearby")
    suspend fun getRecommendationsNearby(): List<PlaceResponseData>
}
