package com.example.gezginasistan.api

import com.example.gezginasistan.model.RegisterRequestData
import com.example.gezginasistan.model.RegisterResponseData
import com.example.gezginasistan.model.TokenResponseData
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    // Login → form-urlencoded
    @FormUrlEncoded
    @POST("users/login")
    suspend fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String = "",
        @Field("client_id") clientId: String? = null,
        @Field("client_secret") clientSecret: String? = null
    ): TokenResponseData

    // Register → JSON body
    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequestData
    ): RegisterResponseData
}
