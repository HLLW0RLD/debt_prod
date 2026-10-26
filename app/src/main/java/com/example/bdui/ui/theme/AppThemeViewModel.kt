package com.example.bdui.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdui.domain.local.PreferenceCache
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppThemeViewModel(
    private val preferenceCache: PreferenceCache
) : ViewModel() {

    val currentTheme: StateFlow<ThemeMode> = preferenceCache.observeSelectedTheme()
        .map { intValue ->
            ThemeMode.fromValue(intValue)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )


    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            preferenceCache.setSelectedTheme(theme.v)
        }
    }

    val isSystemTheme: StateFlow<Boolean> = currentTheme.map { it == ThemeMode.SYSTEM }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isDarkTheme: StateFlow<Boolean> = currentTheme.map { it == ThemeMode.DARK }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isLightTheme: StateFlow<Boolean> = currentTheme.map { it == ThemeMode.LIGHT }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isColoredTheme: StateFlow<Boolean> = currentTheme.map { it == ThemeMode.COLOR }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}