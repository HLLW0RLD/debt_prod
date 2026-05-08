package com.example.debt.data.model.request

// no android dependencies!

data class DebtUpdateRequest(
    val name: String? = null,
    val isMine: Boolean? = null,
    val telegramNick: String? = null,
    val debtAmount: Double? = null,
    val returnDate: String? = null,
    val comment: String? = null
)
