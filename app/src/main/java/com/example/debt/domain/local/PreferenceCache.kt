package com.example.debt.domain.local

import kotlinx.coroutines.flow.Flow

interface PreferenceCache {
    suspend fun saveAuth(userId: String, token: String)
    suspend fun setSelectedTheme(theme: Int)
    suspend fun clearAuth()

    fun observeSelectedTheme(): Flow<Int>
    fun observeUserId(): Flow<String>
    fun observeToken(): Flow<String>
}