package com.example.bdui.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// android dependencies!
// okhttp3

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider.invoke()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}