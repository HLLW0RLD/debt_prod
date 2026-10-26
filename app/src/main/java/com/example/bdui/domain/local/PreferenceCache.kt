package com.example.bdui.domain.local

import kotlinx.coroutines.flow.Flow

interface PreferenceCache {
    suspend fun setSelectedTheme(theme: Int)
    fun observeSelectedTheme(): Flow<Int>
}