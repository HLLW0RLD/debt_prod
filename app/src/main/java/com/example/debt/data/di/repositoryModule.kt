package com.example.debt.app.data.di

import com.example.debt.domain.repo.DebtRepository
import com.example.debt.app.data.repo.DebtRepositoryImpl
import com.example.debt.data.remote.repo.UserRepositoryImpl
import com.example.debt.domain.repo.UserRepository
import org.koin.dsl.module

// android dependencies - cleared!

val repositoryModule = module {

    single<DebtRepository> {
        DebtRepositoryImpl(get())
    }

    single<UserRepository> {
        UserRepositoryImpl(get())
    }
}