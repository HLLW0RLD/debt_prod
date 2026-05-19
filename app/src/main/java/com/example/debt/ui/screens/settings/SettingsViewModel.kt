package com.example.debt.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.debt.domain.local.PreferenceCache
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val preferenceCache: PreferenceCache
) : ViewModel(), KoinComponent {


}