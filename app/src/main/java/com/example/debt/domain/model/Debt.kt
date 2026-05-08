package com.example.debt.domain.model

// android dependencies - cleared!

data class Debt(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val isMine: Boolean = false,
    val telegramNick: String?,
    val debtAmount: Double,
    var loanDate: String = "",
    val returnDate: String? = null, // опционально
    var comment: String? = "", // опционально
    var transactions: List<Transaction> = listOf()
)

data class Transaction(
    val id: String,
    val amount: Double,
    val date: String,
    val type: TransactionType,
    val comment: String?
)

enum class TransactionType(val v: String) {
    DEBT("DEBT"),
    PAYMENT("PAYMENT")
}