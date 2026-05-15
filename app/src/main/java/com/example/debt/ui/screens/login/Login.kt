package com.example.debt.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.debt.app.ui.screens.DebtorsFeed
import com.example.debt.ui.items.CustomLoadingButton
import com.example.debt.ui.items.CustomOutlinedTextField
import com.example.debt.ui.theme.AppColors
import com.example.debt.ui.utils.LocalNavController
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Login

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = koinViewModel()
) {

    val navController = LocalNavController.current

    val state by loginViewModel.loginState.collectAsState()

    var isLogin by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isLogin) "Вход" else "Регистрация",
                color = AppColors.textPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            CustomOutlinedTextField(
                value = loginViewModel.login,
                label = "Login",
                onValueChange = { loginViewModel.login = it },
                singleLine = true,
                keyboardType = KeyboardType.Text,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = !isLogin,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CustomOutlinedTextField(
                    value = loginViewModel.email,
                    label = "Email",
                    onValueChange = { loginViewModel.email = it },
                    singleLine = true,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            CustomOutlinedTextField(
                value = loginViewModel.password,
                label = "Пароль",
                onValueChange = { loginViewModel.password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            CustomLoadingButton(
                textIdl = if (isLogin) "Войти" else "Создать аккаунт",
                isLoading = state is LoginUiState.Loading,
                isError = state is LoginUiState.Error,
                textErr = if(state is LoginUiState.Error) {
                    (state as LoginUiState.Error).message
                } else {
                    ""
                },
                onClick = {
                    if (isLogin) {
                        loginViewModel.login(loginViewModel.login, loginViewModel.password) {
                            navController.navigate(DebtorsFeed)
                        }
                    } else {
                        loginViewModel.register(loginViewModel.login, loginViewModel.email, loginViewModel.password) {
                            navController.navigate(DebtorsFeed)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isLogin) {
                Text(
                    text = "Аккаунт мне пока не нужен",
                    color = AppColors.textSecondary,
                    modifier = Modifier
                        .clickable {
                            showInfoDialog = true
                        }
                        .padding(8.dp)
                )
            }

            Text(
                text = if (isLogin) "Нет аккаунта?" else "Уже есть аккаунт?",
                color = AppColors.accentPrimary,
                modifier = Modifier
                    .clickable { isLogin = !isLogin }
                    .padding(8.dp)
            )
        }

        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showInfoDialog = false
                        }
                    ) {
                        Text("Создать аккаунт")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showInfoDialog = false
                            loginViewModel.saveCacheUser() {
                                navController.navigate(DebtorsFeed)
                            }
                        }
                    ) {
                        Text("Войти без аккаунта")
                    }
                },
                title = {
                    Text("Зачем мне аккаунт?")
                },
                text = {
                    Text("Аккаунт позволит вам не потерять данные после переустановки приложения и переносить их между своими устройствами")
                }
            )
        }
    }
}