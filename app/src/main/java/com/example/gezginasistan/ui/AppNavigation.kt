package com.example.gezginasistan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gezginasistan.ui.LoginScreen
import com.example.gezginasistan.ui.RegisterScreen
import com.example.gezginasistan.ui.MapScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Login ekranı
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    // Token alındığında MapScreen'e yönlendir
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Register ekranı
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Kayıt başarılı → Login ekranına dön
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Map ekranı
        composable("map") {
            MapScreen()
        }
    }
}
