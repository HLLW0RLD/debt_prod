package com.example.debt

import android.app.Application
import com.example.debt.app.data.di.localDbModule
import com.example.debt.app.data.di.repositoryModule
import com.example.debt.data.di.apiModule
import com.example.debt.ui.di.viewModelModule
import com.jakewharton.threetenabp.AndroidThreeTen
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        _appInstance = this

        AndroidThreeTen.init(this)

//        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
//        }

        startKoin {
            androidContext(this@App)
            modules(
                apiModule,
                repositoryModule,
                localDbModule,
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