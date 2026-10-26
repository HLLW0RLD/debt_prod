package com.example.bdui.app.data.di

import com.example.bdui.domain.repo.DebtRepository
import com.example.bdui.app.data.repo.DebtRepositoryImpl
import com.example.bdui.app.data.repo.LocalRepository
import com.example.bdui.app.data.repo.LocalRepositoryImpl
import com.example.bdui.data.remote.repo.AuthRepositoryImpl
import com.example.bdui.domain.repo.AuthRepository
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