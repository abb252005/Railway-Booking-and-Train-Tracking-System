package com.example.railway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val password: String = "",
    val isAdmin: Boolean = false
)
