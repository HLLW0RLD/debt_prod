package com.example.debt.data.remote

import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.data.model.response.DebtResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DebtApi {

    @GET("api/debts/{id}")
    suspend fun getDebtById(
        @Path("id") id: String
    ): Response<DebtResponse>

    @GET("api/debts")
    suspend fun getAllDebts(): Response<List<DebtResponse>>

    @POST("api/debts")
    suspend fun createDebt(
        @Body request: DebtRequest
    ): Response<Unit>

    @POST("api/debts/{id}/add-debt")
    suspend fun addDebt(
        @Path("id") id: String,
        @Body amount: Double
    ): Response<Unit>

    @POST("api/debts/{id}/pay-debt")
    suspend fun payDebt(
        @Path("id") id: String,
        @Body amount: Double
    ): Response<Boolean>

//    @PATCH("api/debts/{user_id}/{id}") -- так нужно
    @PATCH("api/debts/{id}")   // -- так сейчас
    suspend fun updateDebt(
        @Path("id") id: String,
//        @Path("user_id") userId: String,
        @Body request: DebtUpdateRequest
    ): Response<Unit>

    @DELETE("api/debts/{id}")
    suspend fun deleteDebt(
        @Path("id") id: String
    ): Response<Unit>
}