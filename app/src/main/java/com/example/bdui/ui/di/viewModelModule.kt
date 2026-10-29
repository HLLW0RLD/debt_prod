package com.example.bdui.ui.di

import com.example.bdui.ui.theme.AppThemeViewModel
import com.example.bdui.ui.utils.AlertManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { AppThemeViewModel(get()) }
    single { AlertManager() }

}