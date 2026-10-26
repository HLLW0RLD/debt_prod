package com.example.bdui.app.data.di

import com.example.bdui.data.remote.repo.BduiRepositoryImpl
import com.example.bdui.domain.repo.BduiRepository
import org.koin.dsl.module

// android dependencies - cleared!

val repositoryModule = module {

    single<BduiRepository> {
        BduiRepositoryImpl()
    }
}