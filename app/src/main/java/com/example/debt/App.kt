package com.example.debt

import android.app.Application
import com.example.debt.app.data.di.repositoryModule
import com.example.debt.data.di.apiModule
import com.example.debt.ui.di.viewModelModule
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        _appInstance = this

        AndroidThreeTen.init(this)

        startKoin {
            androidContext(this@App)
            modules(
                apiModule,
                repositoryModule,
                viewModelModule,
            )
        }
    }

    companion object {
        private var _appInstance: App? = null
        val appInstance
            get() = _appInstance!!
    }
}