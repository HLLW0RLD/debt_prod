package com.example.debt.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.domain.repo.DebtRepository
import com.example.debt.ui.utils.LogUtils.errorLog
import com.example.debt.domain.Debt
import com.example.debt.domain.TransactionType
import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.data.model.request.TransactionRequest
import com.example.debt.ui.utils.AlertManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FilterType {
    ALL,
    ACTIVE,
    PAID
}

sealed class DebtUiState {
    object Loading : DebtUiState()
    data class Success(val debtors: List<Debt>) : DebtUiState()
    data class Error(val message: String) : DebtUiState()
}

class DebtorsFeedViewModel(
    private val repository: DebtRepository,
    private val alertManager: AlertManager
) : ViewModel() {

    private val _debtors = MutableStateFlow<DebtUiState>(DebtUiState.Loading)
    val debtors = _debtors.asStateFlow()

    private val _debtor = MutableStateFlow<Debt?>(null)
    val debtor = _debtor.asStateFlow()

    private val _refresh = MutableStateFlow<Boolean>(false)
    val refresh = _refresh.asStateFlow()

    init {
        loadAllDebts(false)
    }

    fun showError(text: String, onActionClick: (() -> Unit)? = null) {
        alertManager.showError(text, onActionClick)
    }

    fun showInfo(text: String, onActionClick: (() -> Unit)? = null) {
        alertManager.showInfo(text, onActionClick)
    }

    fun loadAllDebts(refreshing: Boolean = true) {
        viewModelScope.launch {
            if (refreshing) _refresh.value = true else _debtors.value = DebtUiState.Loading

            try {
                repository.getAllDebts().collect { debts ->
                    _debtors.value = DebtUiState.Success(debts)
                    _refresh.value = false
                }
            } catch (e: Exception) {
                _refresh.value = false
                _debtors.value = DebtUiState.Error(e.message ?: "loadAllDebts error")
                errorLog(e.message ?: "loadAllDebts error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDebt(
        name: String,
        isMine: Boolean,
        telegramNick: String?,
        debtAmount: Double,
        returnDate: String?,
        comment: String?
    ) {
        viewModelScope.launch {
            _refresh.value = true

            val request = DebtRequest(
                name = name,
                userId = "userId", // from pref
                isMine = isMine,
                telegramNick = telegramNick,
                debtAmount = debtAmount,
                returnDate = returnDate,
                comment = comment,
                initialTransaction = TransactionRequest(
                    amount = debtAmount,
                    type = if (isMine) TransactionType.DEBT.v else TransactionType.PAYMENT.v,
                    comment = comment
                )
            )

            try {
                repository.createDebt(request)
                loadAllDebts()
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                _refresh.value = false
                errorLog(e)
            }
        }
    }

    fun payDebt(debtorId: String, paymentAmount: Double) {
        viewModelScope.launch {
            _refresh.value = true

            try {
                val paid = repository.payDebt(debtorId, paymentAmount)
                if (paid) showInfo("your debt is paid")
                loadAllDebts()
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                _refresh.value = false
                errorLog(e)
            }
        }
    }

    fun addDebt(debtorId: String, additionalAmount: Double) {
        viewModelScope.launch {
            _refresh.value = true
            try {
                repository.addDebt(debtorId, additionalAmount)
                loadAllDebts()
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                errorLog(e)
            }
        }
    }

    fun updateDebt(
        id: String,
        name: String? = null,
        isMine: Boolean? = null,
        telegramNick: String? = null,
        debtAmount: Double? = null,
        returnDate: String? = null,
        comment: String? = null
    ) {
        viewModelScope.launch {
            _refresh.value = true

            val request = DebtUpdateRequest(
                name = name,
                isMine = isMine,
                telegramNick = telegramNick,
                debtAmount = debtAmount,
                returnDate = returnDate,
                comment = comment
            )

            try {
                repository.updateDebt(id, request)
                loadAllDebts()
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                _refresh.value = false
                errorLog(e)
            }
        }
    }

    fun deleteDebtor(id: String) {
        viewModelScope.launch {
            _refresh.value = true

            try {
                repository.deleteDebt(id)
                loadAllDebts()
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                _refresh.value = false
                errorLog(e)
            }
        }
    }

    fun getDebtorById(id: String) {
        viewModelScope.launch {
            try {
                _debtor.value = repository.getDebtById(id)
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                errorLog(e)
            }
        }
    }
}