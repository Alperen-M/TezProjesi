package com.example.gezginasistan.model

data class Place(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String? = null,
    val address: String? = null,
    val distance: Double? = null
)
