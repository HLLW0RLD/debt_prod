package com.example.debt.data.model.response

import com.example.debt.domain.Debt
import com.example.debt.domain.Transaction
import com.example.debt.domain.TransactionType

// android dependencies - cleared!

data class DebtResponse(
    val id: String,
    val userId: String,
    val name: String,
    val isMine: Boolean,
    val telegramNick: String?,
    val debtAmount: Double,
    val loanDate: String,
    val returnDate: String?,
    val comment: String?,
    val transactions: List<TransactionResponse>
)

data class TransactionResponse(
    val id: String,
    val amount: Double,
    val date: String,
    val type: String, // "DEBT" or "PAYMENT"
    val comment: String?
) {
    fun toModel(): Transaction = Transaction(
        id = id,
        amount = amount,
        date = date,
        type = if (type == TransactionType.DEBT.v) TransactionType.DEBT else TransactionType.PAYMENT,
        comment = comment
    )
}

fun DebtResponse.toModel(): Debt = Debt(
    id = id,
    name = name,
    isMine = isMine,
    telegramNick = telegramNick,
    debtAmount = debtAmount,
    loanDate = loanDate,
    returnDate = returnDate,
    comment = comment,
    transactions = transactions.map { it.toModel() }
)