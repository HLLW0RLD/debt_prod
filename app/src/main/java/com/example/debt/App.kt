package com.example.debt

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.debt.app.data.di.repositoryModule
import com.example.debt.data.di.apiModule
import com.example.debt.ui.di.viewModelModule
import com.example.debt.domain.utils.LogUtils.errorLog
import com.jakewharton.threetenabp.AndroidThreeTen
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.security.KeyStore
import javax.crypto.KeyGenerator

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        _appInstance = this

        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        initTokenCipher()

        startKoin {
            androidContext(this@App)
            modules(
                apiModule,
                repositoryModule,
                viewModelModule,
            )
        }
    }

    private fun initTokenCipher() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val entry = keyStore.getEntry("token_encryption_key", null) as? KeyStore.SecretKeyEntry

            val secretKey = entry?.secretKey ?: run {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        "token_encryption_key",
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                keyGenerator.generateKey()
            }

            TokenCipher.init(secretKey)

        } catch (e: Exception) {
            errorLog(e, "Failed to init TokenCipher")
        }
    }

    companion object {
        private var _appInstance: App? = null
        val appInstance
            get() = _appInstance!!
    }
}