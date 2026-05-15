package com.example.debt.app.data.repo

import com.example.debt.domain.utils.LogUtils.errorLog
import com.example.debt.domain.model.Debt
import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.data.model.response.toModel
import com.example.debt.data.remote.api.DebtApi
import com.example.debt.domain.repo.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// android dependencies - cleared!

class DebtRepositoryImpl(
    private val apiService: DebtApi,
) : DebtRepository {

    override suspend fun getAllDebts(): Flow<List<Debt>> = flow {
        val response = apiService.getAllDebts()
        if (response.isSuccessful) {
            response.body()?.let { debtResponses ->
                emit(debtResponses.map { it.toModel() })
            } ?: emit(emptyList())
        } else {
            errorLog("Failed to load debts: ${response.code()}")
            emit(emptyList())
        }
    }

    override suspend fun createDebt(request: DebtRequest) {
        val response = apiService.createDebt(request)
        if (!response.isSuccessful) {
            errorLog("Failed to create debt: ${response.code()}")
            throw Exception("Failed to create debt: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun addDebt(id: String, amount: Double) {
        val response = apiService.addDebt(id, amount)
        if (!response.isSuccessful) {
            errorLog("Failed to add debt: ${response.code()}")
            throw Exception("Failed to add debt: ${response.code()}")
        }
    }

    override suspend fun payDebt(id: String, amount: Double): Boolean {
        val response = apiService.payDebt(id, amount)
        return if (response.isSuccessful) {
            response.body() ?: false
        } else {
            errorLog("Failed to pay debt: ${response.code()}")
            throw Exception("Failed to pay debt: ${response.code()}")
        }
    }

    override suspend fun updateDebt(id: String, request: DebtUpdateRequest) {
        val response = apiService.updateDebt(id, request)
        if (!response.isSuccessful) {
            errorLog("Failed to update debt: ${response.code()}")
            throw Exception("Failed to update debt: ${response.code()}")
        }
    }

    override suspend fun deleteDebt(id: String) {
        val response = apiService.deleteDebt(id)
        if (!response.isSuccessful) {
            errorLog("Failed to delete debt: ${response.code()}")
            throw Exception("Failed to delete debt: ${response.code()}")
        }
    }

    override suspend fun getDebtById(id: String): Debt? {
        val response = apiService.getDebtById(id)
        return if (response.isSuccessful) {
            response.body()?.toModel()
        } else {
            errorLog("Failed to get debt by id $id: ${response.code()}")
            throw Exception("Failed to get debt by id ${response.code()}")
        }
    }
}