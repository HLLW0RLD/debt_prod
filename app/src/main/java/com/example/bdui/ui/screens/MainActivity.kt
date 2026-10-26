package com.example.bdui.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.bdui.ui.bdui.BduiScreenHost
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.renderer.BduiScreenContent
import com.example.bdui.ui.theme.AppThemeViewModel
import com.example.bdui.ui.theme.AppTheme
import com.example.bdui.ui.utils.LocalNavController
import com.example.bdui.ui.utils.TopAlertHost
import com.example.bdui.ui.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appThemeViewModel: AppThemeViewModel = koinViewModel()
            val selectedTheme by appThemeViewModel.currentTheme.collectAsState()

            AppTheme(
                selectedTheme = selectedTheme
            ) {
                val navController = rememberNavController()

                CompositionLocalProvider(
                    LocalNavController provides navController,
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        NavHost(
                            navController = navController,
                            // Временно запускаем BDUI для разработки.
                            // Когда закончишь тестировать, можно вернуть Login.
                            startDestination = ScreenPage(screenId = "home")
                        ) {
                            // BDUI использует тот же механизм анимации,
                            // что и Login, DebtorsFeed и Settings.
                            animatedComposable<ScreenPage>(
                                navController = navController
                            ) { page ->
                                BduiScreenHost(page = page) { config, route ->
                                    // На этом этапе проверяем, что JSON
                                    // загрузился и экран получил правильный маршрут.
                                    //
                                    // Следующий уровень здесь — BduiScreenContent:
                                    // он отрисует config.body и config.bottomBar
                                    // через BduiRenderer и передаст route.readArgs().
                                    BduiScreenContent(
                                        config = config,
                                        page = route,
                                        navController = navController
                                    )
                                }
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
