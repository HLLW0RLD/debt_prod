package com.example.debt.app.data.repo

import com.example.debt.data.model.db.DebtEntity
import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.domain.model.Debt
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent

interface LocalRepository {
    suspend fun getAllDebts(): Flow<List<Debt>>
    suspend fun createDebt(request: DebtRequest)
    suspend fun addDebt(id: String, amount: Double)
    suspend fun payDebt(id: String, amount: Double): Boolean
    suspend fun updateDebt(id: String, request: DebtUpdateRequest)
    suspend fun deleteDebt(id: String)
    suspend fun getDebtById(id: String): Debt?
}