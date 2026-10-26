package com.example.bdui.app.data.di

import com.example.bdui.data.db.AppDatabase
import com.example.bdui.data.local.PreferenceCacheImpl
import com.example.bdui.domain.local.PreferenceCache
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// android dependencies!
// room

val localDbModule = module {

    single<PreferenceCache> {
        PreferenceCacheImpl(
            context = androidContext()
        )
    }

    single<AppDatabase> {
        AppDatabase.create(get())
    }

    single {
        get<AppDatabase>().debtorDao()
    }
}