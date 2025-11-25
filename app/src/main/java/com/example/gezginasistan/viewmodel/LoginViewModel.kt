package com.example.gezginasistan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.TokenResponseData
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<TokenResponseData?>()
    val loginResult: LiveData<TokenResponseData?> = _loginResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.login(
                    grantType = "password",
                    username = username,
                    password = password
                )
                _loginResult.postValue(response)
            } catch (e: Exception) {
                _errorMessage.postValue("Giriş başarısız: ${e.message}")
            }
        }
    }
}
