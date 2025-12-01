package com.example.gezginasistan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gezginasistan.viewmodel.PlacesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    viewModel: PlacesViewModel = viewModel(),
    navController: NavController   // ✅ NavController parametresi eklendi
) {
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoading by viewModel.isLoadingRecommendations.collectAsState()

    // 🔹 Ekran açıldığında önerileri yükle
    LaunchedEffect(Unit) {
        viewModel.fetchRecommendations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌟 Sana Özel Öneriler") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                recommendations.isEmpty() -> {
                    Text("Öneri bulunamadı. Daha çok yer ziyaret edin!")
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendations) { place ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = place.place_name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Adres: ${place.address}")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("AI Skoru: ${place.ai_score}")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // ✅ MapScreen’e parametrelerle yönlendirme
                                            navController.navigate(
                                                "map?placeId=${place.place_id}&lat=${place.lat}&lon=${place.lon}"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Haritada Gör")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
