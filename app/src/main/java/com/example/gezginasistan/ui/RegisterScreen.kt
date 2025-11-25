package com.example.gezginasistan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.gezginasistan.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    val registerResult by registerViewModel.registerResult.observeAsState(initial = null)
    val errorMessage by registerViewModel.errorMessage.observeAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Ad Soyad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },   // 🔹 Backend email bekliyor
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation() // 🔹 Şifre gizlendi
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { registerViewModel.register(email, password, fullName) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Kayıt Ol") }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zaten hesabın var mı? Giriş yap")
        }

        Spacer(modifier = Modifier.height(16.dp))

        registerResult?.let {
            onRegisterSuccess()
            Text("Kayıt başarılı! Hoş geldin ${it.full_name}")
        }

        errorMessage?.let { error ->
            Text("Hata: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}
