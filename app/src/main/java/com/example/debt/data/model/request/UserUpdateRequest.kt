package com.example.debt.data.model.request

data class UserUpdateRequest(
    val email: String? = null,
    val login: String? = null,
    val password: String? = null
)
