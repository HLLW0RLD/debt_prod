package com.example.bdui.data.di

import com.example.bdui.data.remote.api.AuthApi
import com.example.bdui.data.remote.api.DebtApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// android dependencies!
// retrofit2, okhttp3

val apiModule = module {

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

//    single {
//        AuthInterceptor {
//            runBlocking {
//                PersonalDataManager.getTokenFromCache()
//            }
//        }
//    }

    single {
        OkHttpClient.Builder()
//            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://feel-u.ru/debts-api/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(DebtApi::class.java) }
    single { get<Retrofit>().create(AuthApi::class.java) }
}