package com.example.gezginasistan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gezginasistan.ui.LoginScreen
import com.example.gezginasistan.ui.RegisterScreen
import com.example.gezginasistan.ui.MapScreen
import com.example.gezginasistan.ui.RecommendationsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 🔐 Login ekranı
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    // Başarılı giriş sonrası MapScreen'e yönlendir
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 📝 Register ekranı
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Kayıt sonrası login ekranına dön
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 🌟 Recommendations ekranı
        composable("recommendations") {
            // Burada navController'ı parametre olarak verelim ki navigate edebilsin
            RecommendationsScreen(navController = navController)
        }

        // 🗺️ Map ekranı — parametrelerle
        composable(
            route = "map?placeId={placeId}&lat={lat}&lon={lon}",
            arguments = listOf(
                navArgument("placeId") { defaultValue = "" },
                navArgument("lat") { defaultValue = "" },
                navArgument("lon") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val placeIdArg = backStackEntry.arguments?.getString("placeId").orEmpty()
            val latArg = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lonArg = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()

            MapScreen(
                navController = navController,
                selectedPlaceId = placeIdArg.ifEmpty { null },
                selectedLat = latArg,
                selectedLon = lonArg
            )
        }
    }
}
