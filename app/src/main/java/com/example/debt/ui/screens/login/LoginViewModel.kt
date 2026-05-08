package com.example.debt.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.debt.domain.utils.isValidEmail
import com.example.debt.ui.utils.AlertManager

class LoginViewModel(
//    private val repository: UserRepository,
    private val alertManager: AlertManager
): ViewModel() {

    var login by mutableStateOf("")
    var password by mutableStateOf("")

    val isLoginValid: Boolean
        get() = isValidEmail(login) &&
                password.isNotBlank()

    fun login(email: String, password: String) {

    }

    fun register(email: String, password: String) {

    }
}