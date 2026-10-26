package com.example.bdui.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.bdui.domain.local.PreferenceCache
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val preferenceCache: PreferenceCache
) : ViewModel(), KoinComponent {


}