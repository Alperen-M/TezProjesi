package com.example.gezginasistan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var passwordVisible by remember { mutableStateOf(false) }

    val loginResult by loginViewModel.loginResult.observeAsState(initial = null)
    val errorMessage by loginViewModel.errorMessage.observeAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gezgin Asistan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 📧 E-posta alanı
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔒 Şifre alanı (göster/gizle ikonu ile)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔘 Giriş Yap butonu
        Button(
            onClick = { loginViewModel.login(username = email, password = password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Yap")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔗 Kayıt Ol yönlendirmesi
        TextButton(
            onClick = { onNavigateToRegister() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hesabın yok mu? Kayıt ol")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Sonuç mesajları
        loginResult?.let { tokenResponse ->
            onLoginSuccess(tokenResponse.access_token)
            Text("Giriş başarılı!", color = MaterialTheme.colorScheme.primary)
        }

        errorMessage?.let { error ->
            Text("Hata: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}
