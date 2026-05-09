package com.example.debt.data.model.request

import java.util.UUID

data class UserRegistration(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val login: String,
    val password: String,
)