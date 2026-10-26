package com.example.bdui.domain.repo

import com.example.bdui.data.model.request.DebtRequest
import com.example.bdui.data.model.request.DebtUpdateRequest
import com.example.bdui.domain.model.Debt
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    suspend fun getAllDebts(): Flow<List<Debt>>
    suspend fun createDebt(request: DebtRequest)
    suspend fun addDebt(id: String, amount: Double)
    suspend fun payDebt(id: String, amount: Double): Boolean
    suspend fun updateDebt(id: String, request: DebtUpdateRequest)
    suspend fun deleteDebt(id: String)
    suspend fun getDebtById(id: String): Debt?
}