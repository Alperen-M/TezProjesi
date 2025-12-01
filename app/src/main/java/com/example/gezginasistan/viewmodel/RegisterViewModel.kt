package com.example.gezginasistan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.AuthApi
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.RegisterRequestData
import com.example.gezginasistan.model.RegisterResponseData
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<RegisterResponseData?>()
    val registerResult: LiveData<RegisterResponseData?> = _registerResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ✅ Doğru kullanım
    private val authApi: AuthApi = RetrofitClient.create<AuthApi>()

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            try {
                val request = RegisterRequestData(email, password, fullName)
                val response = authApi.register(request)

                _registerResult.value = response
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Bilinmeyen hata"
                _registerResult.value = null
            }
        }
    }
}
