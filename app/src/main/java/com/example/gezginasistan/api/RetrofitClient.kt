package com.example.gezginasistan.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Backend base URL
    private const val BASE_URL = "https://tezprojesi.onrender.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Login/Register işlemleri için
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // Places işlemleri için
    val placesApi: PlacesApi by lazy {
        retrofit.create(PlacesApi::class.java)
    }
}
