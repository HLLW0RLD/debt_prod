package com.example.bdui.data.remote.api

import com.example.bdui.data.model.request.UserLogin
import com.example.bdui.data.model.request.UserRegistration
import com.example.bdui.data.model.request.UserUpdateRequest
import com.example.bdui.data.model.response.UserAuth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// android dependencies!
// retrofit2

interface AuthApi {

    @POST("api/debts/register-guest")
    suspend fun registerGuest(
        @Body id: String
    ): Response<UserAuth>

    @POST("api/debts/login")
    suspend fun login(
        @Body data: UserLogin
    ): Response<UserAuth>

    @POST("api/debts/register")
    suspend fun register(
        @Body data: UserRegistration
    ): Response<UserAuth>

    @PATCH("api/debts/update/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body update: UserUpdateRequest // use saved id
    ): Response<Unit>

//    @DELETE("api/debts/user/{id}")
//    suspend fun deleteAccount(
//        @Path("id") id: String
//    ): Response<Unit>
//
//    // с подтверждением пароля
//    @POST("api/debts/user/{id}/delete")
//    suspend fun deleteAccount(
//        @Path("id") id: String,
//        @Body password: String
//    ): Response<Unit>
}