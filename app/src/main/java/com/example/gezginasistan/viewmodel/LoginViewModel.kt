package com.example.gezginasistan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.AuthApi
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.api.TokenProvider
import com.example.gezginasistan.datastore.AuthDataStore
import com.example.gezginasistan.model.LoginRequestData
import com.example.gezginasistan.model.TokenResponseData
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authDataStore: AuthDataStore? = null // DI yoksa opsiyonel
) : ViewModel() {

    private val _loginResult = MutableLiveData<TokenResponseData?>()
    val loginResult: LiveData<TokenResponseData?> = _loginResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val authApi: AuthApi = RetrofitClient.create()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authApi.login(
                    grantType = "password",
                    username = username,
                    password = password,
                    scope = "",
                    clientId = null,
                    clientSecret = null
                )

                // Başarılı yanıt direkt TokenResponseData
                TokenProvider.setToken(response.access_token)
                authDataStore?.saveAccessToken(response.access_token)

                _loginResult.value = response
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Bilinmeyen hata"
                _loginResult.value = null
            }
        }
    }

}
