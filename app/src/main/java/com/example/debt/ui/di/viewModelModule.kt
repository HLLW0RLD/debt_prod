package com.example.debt.ui.di


import com.example.debt.ui.screens.login.LoginViewModel
import com.example.debt.ui.screens.main.DebtorsFeedViewModel
import com.example.debt.ui.screens.settings.SettingsViewModel
import com.example.debt.ui.utils.AlertManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get()) }
    viewModel { DebtorsFeedViewModel(get(), get()) }
    viewModel { SettingsViewModel() }

    single { AlertManager() }
}