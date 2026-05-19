package com.example.debt.ui.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.domain.repo.DebtRepository
import com.example.debt.domain.utils.LogUtils.errorLog
import com.example.debt.domain.model.Debt
import com.example.debt.domain.model.TransactionType
import com.example.debt.data.model.request.DebtRequest
import com.example.debt.data.model.request.DebtUpdateRequest
import com.example.debt.data.model.request.TransactionRequest
import com.example.debt.domain.local.PreferenceCache
import com.example.debt.domain.repo.AuthRepository
import com.example.debt.ui.screens.login.LoginUiState
import com.example.debt.ui.theme.ThemeMode
import com.example.debt.ui.utils.AlertManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val authRepository: AuthRepository,
    private val debtRepository: DebtRepository,
    private val preferenceCache: PreferenceCache,
    private val alertManager: AlertManager
) : ViewModel() {

    private val _debtors = MutableStateFlow<DebtUiState>(DebtUiState.Loading)
    val debtors = _debtors.asStateFlow()

    private val _debtor = MutableStateFlow<Debt?>(null)
    val debtor = _debtor.asStateFlow()

    private val _refresh = MutableStateFlow<Boolean>(false)
    val refresh = _refresh.asStateFlow()

    // todo move to splashViewModel
    val userId: StateFlow<String> = preferenceCache.observeUserId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        if (userId.value.isEmpty()) {
            // todo move to splashViewModel
            registerGuest {
                loadAllDebts(false)
            }
        } else {
            loadAllDebts(false)
        }
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
                debtRepository.getAllDebts().collect { debts ->
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
                debtRepository.createDebt(request)
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
                val paid = debtRepository.payDebt(debtorId, paymentAmount)
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
                debtRepository.addDebt(debtorId, additionalAmount)
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
                debtRepository.updateDebt(id, request)
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
                debtRepository.deleteDebt(id)
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
                _debtor.value = debtRepository.getDebtById(id)
            } catch (e: Exception) {
                showError(e.message ?: "Error")
                errorLog(e)
            }
        }
    }

    // todo move to splashViewModel
    fun registerGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _debtors.value = DebtUiState.Loading

            val result = authRepository.registerGuest()

            // api
            result.onSuccess {

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }

            result.onFailure {
                _debtors.value = DebtUiState.Error("${result.exceptionOrNull()?.message}")

                withContext(Dispatchers.Main) {
                    showError("${result.exceptionOrNull()?.message}")
                }
            }

//            moc
//            delay(3000)
//            onSuccess()
        }
    }
}