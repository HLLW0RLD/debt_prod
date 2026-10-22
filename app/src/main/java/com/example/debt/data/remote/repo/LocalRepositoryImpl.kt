package com.example.debt.app.data.repo

import com.example.debt.data.local.db.DebtorDao
import com.example.debt.data.model.db.DebtEntity
import com.example.debt.data.model.db.toEntity
import com.example.debt.data.model.db.toModel
import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.domain.local.PreferenceCache
import com.example.debt.domain.model.Debt
import com.example.debt.domain.model.Transaction
import com.example.debt.domain.model.TransactionType
import com.example.debt.domain.model.addDebt
import com.example.debt.domain.model.addPayment
import com.example.debt.domain.repo.DebtRepository
import com.example.debt.domain.utils.LogUtils.debugLog
import com.example.debt.domain.utils.LogUtils.errorLog
import com.example.debt.domain.utils.getCurrentDateTimeUTC
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.also
import kotlin.let
import kotlin.run

class LocalRepositoryImpl(
    private val debtorDao: DebtorDao
) : LocalRepository {

    override suspend fun getAllDebts(): Flow<List<Debt>> {
        return debtorDao.getAllDebtors().map { debts ->
            debts.map { it.toModel() }
        }
    }

    override suspend fun createDebt(request: DebtRequest) {
        val initialTransaction = Transaction(
            id = request.initialTransaction.id,
            amount = request.initialTransaction.amount,
            date = request.initialTransaction.date,
            type = TransactionType.valueOf(request.initialTransaction.type),
            comment = request.initialTransaction.comment
        )

        val newDebt = Debt(
            id = request.id,
            userId = request.userId,
            name = request.name,
            isMine = request.isMine,
            telegramNick = request.telegramNick,
            debtAmount = request.debtAmount,
            loanDate = request.loanDate,
            returnDate = request.returnDate,
            comment = request.comment,
            transactions = listOf(initialTransaction)
        )

        val existingEntity = debtorDao.findAnyDebt(
            userId = request.userId,
            name = request.name,
            telegramNick = request.telegramNick
        )

        val existingDebt = existingEntity?.toModel()

        if (existingDebt == null) {
            debtorDao.insert(newDebt.toEntity())
            return
        }

        if (existingDebt.isMine == newDebt.isMine) {
            val updatedDebt = existingDebt.copy(
                debtAmount = existingDebt.debtAmount + newDebt.debtAmount,
                transactions = existingDebt.transactions + initialTransaction
            )

            debtorDao.updateDebt(updatedDebt.toEntity())
            return
        }

        val amountToSettle = minOf(
            newDebt.debtAmount,
            existingDebt.debtAmount
        )

        val updatedExistingDebt = if (existingDebt.isMine) {
            existingDebt.addPayment(amountToSettle)
        } else {
            existingDebt.addDebt(amountToSettle)
        }

        saveOrDeleteEmptyDebt(updatedExistingDebt)

        val remainingAmount = newDebt.debtAmount - amountToSettle

        if (remainingAmount > 0.0) {
            val remainingDebt = newDebt.copy(
                debtAmount = remainingAmount,
                transactions = listOf(
                    initialTransaction.copy(amount = remainingAmount)
                )
            )

            debtorDao.insert(remainingDebt.toEntity())
        }
    }

    override suspend fun addDebt(id: String, amount: Double) {
        require(amount > 0.0) { "Amount must be greater than zero" }

        val debt = debtorDao.getDebtorById(id)?.toModel() ?: return

        if (debt.isMine) {
            val mutualDebt = debtorDao.findDebt(
                userId = debt.userId,
                name = debt.name,
                telegramNick = debt.telegramNick,
                isMine = false
            )?.toModel()

            if (mutualDebt != null) {
                val amountToSettle = minOf(amount, mutualDebt.debtAmount)

                saveOrDeleteEmptyDebt(
                    mutualDebt.addPayment(amountToSettle)
                )

                val remaining = amount - amountToSettle

                if (remaining > 0.0) {
                    debtorDao.updateDebt(
                        debt.addDebt(remaining).toEntity()
                    )
                }

                return
            }
        }

        debtorDao.updateDebt(debt.addDebt(amount).toEntity())
    }

    override suspend fun payDebt(id: String, amount: Double): Boolean {
        require(amount > 0.0) { "Amount must be greater than zero" }

        val debt = debtorDao.getDebtorById(id)?.toModel()
            ?: return false

        if (!debt.isMine) {
            val mutualDebt = debtorDao.findDebt(
                userId = debt.userId,
                name = debt.name,
                telegramNick = debt.telegramNick,
                isMine = true
            )?.toModel()

            if (mutualDebt != null) {
                val amountToSettle = minOf(
                    amount,
                    mutualDebt.debtAmount,
                    debt.debtAmount
                )

                saveOrDeleteEmptyDebt(
                    mutualDebt.addPayment(amountToSettle)
                )

                saveOrDeleteEmptyDebt(
                    debt.addPayment(amountToSettle)
                )

                val remaining = amount - amountToSettle

                if (remaining > 0.0) {
                    handleRemainingPayment(
                        debt.copy(
                            debtAmount = debt.debtAmount - amountToSettle,
                            transactions = debt.transactions + Transaction(
                                id = UUID.randomUUID().toString(),
                                amount = amountToSettle,
                                date = getCurrentDateTimeUTC(),
                                type = TransactionType.PAYMENT,
                                comment = debt.comment
                            )
                        ),
                        remaining
                    )
                }

                return true
            }
        }

        handleRemainingPayment(debt, amount)
        return true
    }

    override suspend fun updateDebt(
        id: String,
        request: DebtUpdateRequest
    ) {
        val debt = debtorDao.getDebtorById(id)?.toModel() ?: return

        val updatedDebt = debt.copy(
            name = request.name ?: debt.name,
            isMine = request.isMine ?: debt.isMine,
            telegramNick = request.telegramNick ?: debt.telegramNick,
            debtAmount = request.debtAmount ?: debt.debtAmount,
            returnDate = request.returnDate ?: debt.returnDate,
            comment = request.comment ?: debt.comment
        )

        debtorDao.updateDebt(updatedDebt.toEntity())
    }

    override suspend fun deleteDebt(id: String) {
        debtorDao.deleteDebtor(id)
    }

    override suspend fun getDebtById(id: String): Debt? {
        return debtorDao.getDebtorById(id)?.toModel()
    }

    private suspend fun handleRemainingPayment(
        debt: Debt,
        amount: Double
    ) {
        if (amount > debt.debtAmount) {
            handleOverpayment(debt, amount)
        } else {
            saveOrDeleteEmptyDebt(debt.addPayment(amount))
        }
    }

    private suspend fun handleOverpayment(
        debt: Debt,
        amount: Double
    ) {
        val overpayment = amount - debt.debtAmount

        val updatedDebt = debt.addPayment(debt.debtAmount)
        saveOrDeleteEmptyDebt(updatedDebt)

        val comment = "Автоматически создано при переплате\n${debt.comment}"

        val reverseDebt = Debt(
            id = UUID.randomUUID().toString(),
            userId = debt.userId,
            name = debt.name,
            isMine = !debt.isMine,
            telegramNick = debt.telegramNick,
            debtAmount = overpayment,
            loanDate = getCurrentDateTimeUTC(),
            returnDate = null,
            comment = comment,
            transactions = listOf(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = overpayment,
                    date = getCurrentDateTimeUTC(),
                    type = TransactionType.DEBT,
                    comment = comment
                )
            )
        )

        debtorDao.insert(reverseDebt.toEntity())
    }

    private suspend fun saveOrDeleteEmptyDebt(debt: Debt) {
//        if (
//            debt.debtAmount <= 0.0
//        ) {
//            debtorDao.deleteDebtor(debt.id)
//        } else {
            debtorDao.updateDebt(debt.toEntity())
//        }
    }
}