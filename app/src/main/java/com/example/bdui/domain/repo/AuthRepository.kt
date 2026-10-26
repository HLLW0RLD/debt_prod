package com.example.bdui.domain.repo

import com.example.bdui.data.model.response.UserAuth

interface AuthRepository {

    suspend fun login(login: String, password: String): Result<UserAuth?>
    suspend fun register(login: String, email: String, password: String): Result<UserAuth?>
    suspend fun registerGuest(): Result<UserAuth?>
    suspend fun update(id: String, login: String, email: String, password: String): Result<Unit?>
}