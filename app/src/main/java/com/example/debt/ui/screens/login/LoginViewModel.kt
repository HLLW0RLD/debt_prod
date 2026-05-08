package com.example.debt.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.domain.utils.isValidEmail
import com.example.debt.ui.utils.AlertManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
//    private val repository: UserRepository,
    private val alertManager: AlertManager
): ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state = _state.asStateFlow()

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
            _state.value = LoginUiState.Loading
            delay(3000)
//            onSuccess()
        }
    }

    fun register(login: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            delay(3000)
//            onSuccess()
        }
    }

    fun saveCacheUser(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            onSuccess()
        }
    }
}