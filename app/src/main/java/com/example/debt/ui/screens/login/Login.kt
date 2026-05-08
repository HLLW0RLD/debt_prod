package com.example.debt.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.debt.app.ui.screens.DebtorsFeed
import com.example.debt.ui.items.CustomOutlinedTextField
import com.example.debt.ui.items.CustomTextButton
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
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    var isLogin by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                value = email,
                label = "Email",
                onValueChange = { email = it },
                singleLine = true,
                keyboardType = KeyboardType.Email,
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomOutlinedTextField(
                value = password,
                label = "Пароль",
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()

                        if (isLogin) {
                            loginViewModel.login(email, password)
                        } else {
                            loginViewModel.register(email, password)
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextButton(
                text = if (isLogin) "Войти" else "Создать аккаунт",
                onClick = {
                    if (isLogin) {
                        loginViewModel.login(email, password)
                    } else {
                        loginViewModel.register(email, password)
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
                            navController.navigate(DebtorsFeed)
                        }
                    ) {
                        Text("войти без аккаунта")
                    }
                },
                title = {
                    Text("Зачем мне аккаунт?")
                },
                text = {
                    Text("Аккаунт позволит вам не потерять данные после переустановки приложения")
                }
            )
        }
    }
}