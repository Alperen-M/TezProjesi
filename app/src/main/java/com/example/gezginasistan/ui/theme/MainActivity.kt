package com.example.gezginasistan.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gezginasistan.ui.LocationPermissionRequest
import com.example.gezginasistan.ui.MapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationPermissionRequest(
                onPermissionGranted = {
                    MapScreen() // ✅ Bu da @Composable olduğu için artık hata vermez
                }
            )
        }
    }
}