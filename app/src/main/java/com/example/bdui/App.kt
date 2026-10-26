package com.example.bdui

import android.app.Application
import com.example.bdui.app.data.di.localCacheModule
import com.example.bdui.app.data.di.repositoryModule
import com.example.bdui.data.di.apiModule
import com.example.bdui.ui.di.viewModelModule
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
                localCacheModule,
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