package com.example.debt.ui.screens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.debt.app.ui.screens.DebtorsFeed
import com.example.debt.app.ui.screens.DebtorsFeedScreen
import com.example.debt.ui.screens.login.Login
import com.example.debt.ui.screens.login.LoginScreen
import com.example.debt.ui.screens.settings.Settings
import com.example.debt.ui.screens.settings.SettingsScreen
import com.example.debt.ui.theme.AppTheme
import com.example.debt.ui.utils.LocalNavController
import com.example.debt.ui.utils.TopAlertHost
import com.example.debt.ui.utils.animatedComposable

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()

                CompositionLocalProvider(
                    LocalNavController provides navController,
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Login
                        ) {
                            animatedComposable<Login>(
                                navController = navController
                            ) {
                                LoginScreen()
                            }
                            animatedComposable<DebtorsFeed>(
                                navController = navController
                            ) {
                                DebtorsFeedScreen()
                            }
                            animatedComposable<Settings>(
                                navController = navController
                            ) {
                                SettingsScreen()
                            }
                        }

                        TopAlertHost(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                }
            }
        }
    }
}
