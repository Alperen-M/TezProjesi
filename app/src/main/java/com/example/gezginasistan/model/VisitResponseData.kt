package com.example.gezginasistan.model

data class VisitResponseData(
    val place_id: String,
    val place_name: String,
    val place_category: String,
    val id: Int,
    val created_at: String,
    val user_id: Int
)
