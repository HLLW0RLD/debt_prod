package com.example.debt.app.data.di

import com.example.debt.domain.repo.DebtRepository
import com.example.debt.app.data.repo.DebtRepositoryImpl
import com.example.debt.app.data.repo.LocalRepository
import com.example.debt.app.data.repo.LocalRepositoryImpl
import com.example.debt.data.remote.repo.AuthRepositoryImpl
import com.example.debt.domain.repo.AuthRepository
import org.koin.dsl.module

// android dependencies - cleared!

val repositoryModule = module {

    single<DebtRepository> {
        DebtRepositoryImpl(get())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }

    single<LocalRepository> {
        LocalRepositoryImpl(get())
    }
}