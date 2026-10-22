package com.example.debt.data.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.debt.domain.model.Debt
import com.example.debt.domain.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "debtors")
data class DebtEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val name: String,
    val isMine: Boolean = false,
    val telegramNick: String? = null,
    val debtAmount: Double,
    val loanDate: String = "",
    val returnDate: String? = null,
    var comment: String? = "",
    @ColumnInfo(name = "transactions")
    val transactionsJson: String = ""
)
private val gson = Gson()

fun DebtEntity.toModel(): Debt {
    val type = object : TypeToken<List<Transaction>>() {}.type

    val transactions = runCatching {
        gson.fromJson<List<Transaction>>(transactionsJson, type) ?: emptyList()
    }.getOrDefault(emptyList())

    return Debt(
        id = id,
        userId = userId,
        name = name,
        isMine = isMine,
        telegramNick = telegramNick,
        debtAmount = debtAmount,
        loanDate = loanDate,
        returnDate = returnDate,
        comment = comment,
        transactions = transactions
    )
}

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id,
    userId = userId,
    name = name,
    isMine = isMine,
    telegramNick = telegramNick,
    debtAmount = debtAmount,
    loanDate = loanDate,
    returnDate = returnDate,
    comment = comment,
    transactionsJson = gson.toJson(transactions)
)
