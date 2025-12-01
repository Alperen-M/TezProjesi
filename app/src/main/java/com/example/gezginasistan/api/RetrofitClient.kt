package com.example.gezginasistan.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Token'ı memory'de tutan basit provider.
// Login sonrası setToken çağrılır; logout'ta clearToken.
object TokenProvider {
    @Volatile
    private var token: String? = null

    fun setToken(value: String?) {
        token = value
    }

    fun getToken(): String? = token
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val token = TokenProvider.getToken()

        val newRequest = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(newRequest)
    }
}

object RetrofitClient {
    // Backend base URL'ini kendi ortamına göre güncelle
    private const val BASE_URL = "https://tezprojesi.onrender.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
