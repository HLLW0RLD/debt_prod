package com.example.debt.domain.repo

interface UserRepository {

    fun login(email: String, password: String)
    fun register(email: String, password: String)
}