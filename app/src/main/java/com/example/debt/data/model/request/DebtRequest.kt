package com.example.debt.data.model.request

import com.example.debt.domain.utils.getCurrentDateTimeUTC
import java.util.UUID

// android dependencies - cleared!

data class DebtRequest(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val isMine: Boolean,
    val telegramNick: String?,
    val debtAmount: Double,
    val loanDate: String = getCurrentDateTimeUTC(),
    val returnDate: String?,
    val comment: String?,
    val initialTransaction: TransactionRequest
)

data class TransactionRequest(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val date: String = getCurrentDateTimeUTC(),
    val type: String, // "DEBT" or "PAYMENT"
    val comment: String? = null
)