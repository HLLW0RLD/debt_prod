package com.example.debt.data.remote.repo

import com.example.debt.data.remote.api.UserApi
import com.example.debt.domain.repo.UserRepository

// android dependencies - cleared!

class UserRepositoryImpl(
    private val apiService: UserApi,
): UserRepository {

    override fun login(email: String, password: String) {

    }

    override fun register(email: String, password: String) {

    }

    override fun update(
        id: String,
        login: String,
        email: String,
        password: String
    ) {

    }
}