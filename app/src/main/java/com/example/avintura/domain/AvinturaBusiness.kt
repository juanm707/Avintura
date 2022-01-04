package com.example.avintura.domain

data class AvinturaBusiness(
    val id: String,
    val name: String,
    val rating: Float,
    val imageUrl: String,
    val reviewCount: Int
)