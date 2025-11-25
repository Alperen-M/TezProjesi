package com.example.gezginasistan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.gezginasistan.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginResult by loginViewModel.loginResult.observeAsState(initial = null)
    val errorMessage by loginViewModel.errorMessage.observeAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },   // 🔹 Artık sadece e-posta
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginViewModel.login(username = email, password = password) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Giriş Yap") }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { onNavigateToRegister() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesabın yok mu? Kayıt ol")
        }

        Spacer(modifier = Modifier.height(16.dp))

        loginResult?.let { tokenResponse ->
            onLoginSuccess(tokenResponse.access_token)
            Text("Giriş başarılı! Token: ${tokenResponse.access_token}")
        }

        errorMessage?.let { error ->
            Text("Hata: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}
