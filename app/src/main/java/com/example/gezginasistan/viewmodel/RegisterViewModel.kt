package com.example.gezginasistan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gezginasistan.api.RetrofitClient
import com.example.gezginasistan.model.RegisterRequestData
import com.example.gezginasistan.model.RegisterResponseData
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class RegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<RegisterResponseData?>()
    val registerResult: LiveData<RegisterResponseData?> = _registerResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            try {
                val request = RegisterRequestData(
                    email = email,
                    password = password,
                    full_name = fullName
                )
                val response = RetrofitClient.authApi.register(request)
                _registerResult.postValue(response)
            } catch (e: Exception) {
                _errorMessage.postValue("Kayıt başarısız: ${e.message}")
            }
        }
    }
}
