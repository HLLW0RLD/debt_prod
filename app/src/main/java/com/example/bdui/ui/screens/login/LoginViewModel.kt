package com.example.bdui.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdui.domain.repo.AuthRepository
import com.example.bdui.domain.utils.isValidEmail
import com.example.bdui.ui.utils.AlertManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.onFailure
import kotlin.onSuccess

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val alertManager: AlertManager
): ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState = _loginState.asStateFlow()

    var email by mutableStateOf("")
    var login by mutableStateOf("")
    var password by mutableStateOf("")

    val isEmailValid: Boolean
        get() = isValidEmail(email)

    val isPasswordValid: Boolean
        get() = password.isNotBlank() && password.length >= 8

    val isLoginValid: Boolean
        get() = login.isNotBlank()


    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = repository.login(email, password)

            // api
            result.onSuccess {
                _loginState.value = LoginUiState.Idle

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }

            result.onFailure {
                withContext(Dispatchers.Main) {
                    showError("${result.exceptionOrNull()?.message}")
                }
            }

//            moc
//            delay(3000)
//            onSuccess()
        }
    }

    fun register(login: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = repository.register(
                login = login,
                email = email,
                password = password)

            // api
            result.onSuccess {
                _loginState.value = LoginUiState.Idle

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }

            result.onFailure {
                withContext(Dispatchers.Main) {
                    showError("${result.exceptionOrNull()?.message}")
                }
            }

//            moc
//            delay(3000)
//            onSuccess()
        }
    }

    fun showError(text: String, onActionClick: (() -> Unit)? = null) {
        alertManager.showError(text, onActionClick)
    }
}