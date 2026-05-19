package com.example.debt.data.remote.repo

import com.example.debt.data.model.request.UserLogin
import com.example.debt.data.model.request.UserRegistration
import com.example.debt.data.model.request.UserUpdateRequest
import com.example.debt.data.model.response.UserAuth
import com.example.debt.data.remote.api.AuthApi
import com.example.debt.domain.local.PreferenceCache
import com.example.debt.domain.repo.AuthRepository
import com.example.debt.domain.utils.LogUtils.errorLog
import java.util.UUID

// android dependencies - cleared!

class AuthRepositoryImpl(
    private val apiService: AuthApi,
    private val preferenceCache: PreferenceCache, // todo move to useCase
): AuthRepository {

    override suspend fun registerGuest(): Result<UserAuth?> {
        val id = UUID.randomUUID().toString()

        return try {
            val response = apiService.registerGuest(id)

            if (response.isSuccessful) {
                val userAuth = response.body()
                // todo move to splash useCase
                userAuth?.let {
                    preferenceCache.saveAuth(
                        userId = it.userId ?: "",
                        token = it.token ?: ""
                    )
                }

                Result.success(userAuth)
            } else {
                val errorBody = response.errorBody()?.string()
                errorLog("Ошибка сервера: ${response.code()} - $errorBody")

                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            errorLog("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Нет подключения к интернету"))
        }
    }

    override suspend fun login(login: String, password: String): Result<UserAuth?> {

        val request = UserLogin(login, password)

        return try {
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val userAuth = response.body()
                // todo move to profile useCase
                userAuth?.let {
                    preferenceCache.saveAuth(
                        userId = it.userId ?: "",
                        token = it.token ?: ""
                    )
                }

                Result.success(userAuth)
            } else {
                val errorBody = response.errorBody()?.string()
                errorLog("Ошибка сервера: ${response.code()} - $errorBody")

                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            errorLog("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Нет подключения к интернету"))
        }
    }

    override suspend fun register(login: String, email: String, password: String): Result<UserAuth?> {
        val request = UserRegistration(
            email = email,
            login = login,
            password = password
        )

        return try {
            val response = apiService.register(request)

            if (response.isSuccessful) {
                val userAuth = response.body()
                // todo move to profile useCase
                userAuth?.let {
                    preferenceCache.saveAuth(
                        userId = it.userId ?: "",
                        token = it.token ?: ""
                    )
                }

                Result.success(userAuth)
            } else {
                val errorBody = response.errorBody()?.string()
                errorLog("Ошибка сервера: ${response.code()} - $errorBody")

                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            errorLog("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Нет подключения к интернету"))
        }
    }

    override suspend fun update(
        id: String,
        login: String,
        email: String,
        password: String
    ): Result<Unit?> {

        val request = UserUpdateRequest(
            email = email,
            login = login,
            password = password
        )

        return try {
            val response = apiService.update(
                id = id,
                update = request
            )

            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                val errorBody = response.errorBody()?.string()
                errorLog("Ошибка сервера: ${response.code()} - $errorBody")

                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            errorLog("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Нет подключения к интернету"))
        }
    }
}