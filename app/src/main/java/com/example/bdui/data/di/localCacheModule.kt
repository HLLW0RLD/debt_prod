package com.example.bdui.app.data.di

import com.example.bdui.data.local.BduiStorageImpl
import com.example.bdui.data.local.PreferenceCacheImpl
import com.example.bdui.domain.local.BduiStorage
import com.example.bdui.domain.local.PreferenceCache
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localCacheModule = module {

    single<PreferenceCache> {
        PreferenceCacheImpl(
            context = androidContext()
        )
    }

    single<BduiStorage> {
        BduiStorageImpl(androidContext())
    }
}