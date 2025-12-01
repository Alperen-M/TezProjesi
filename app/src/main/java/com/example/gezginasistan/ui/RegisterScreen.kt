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
import com.example.gezginasistan.viewmodel.RegisterViewModel


@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val registerResult by registerViewModel.registerResult.observeAsState()
    val errorMessage by registerViewModel.errorMessage.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kayıt Ol",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 👤 Ad Soyad
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Ad Soyad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 📧 E-posta
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔒 Şifre (göster/gizle ikonu ile)
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

        // 🔘 Kayıt Ol butonu
        Button(
            onClick = { registerViewModel.register(email, password, fullName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kayıt Ol")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Sonuç mesajları
        registerResult?.let {
            Text("Kayıt başarılı! Hoş geldin ${it.full_name}", color = MaterialTheme.colorScheme.primary)
            onRegisterSuccess()
        }

        errorMessage?.let {
            Text("Hata: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
