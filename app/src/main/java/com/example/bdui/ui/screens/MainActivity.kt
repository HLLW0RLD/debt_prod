package com.example.bdui.ui.screens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bdui.ui.bdui.BduiScreenHost
import com.example.bdui.ui.bdui.model.ScreenConfig
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.renderer.chrome.BduiAppBarRegistry
import com.example.bdui.ui.bdui.renderer.chrome.BduiBottomBarRegistry
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContent
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import com.example.bdui.ui.theme.AppScaffold
import com.example.bdui.ui.theme.AppThemeViewModel
import com.example.bdui.ui.theme.AppTheme
import com.example.bdui.ui.theme.BottomBarState
import com.example.bdui.ui.theme.LocalBottomBarState
import com.example.bdui.ui.utils.LocalNavController
import com.example.bdui.ui.utils.TopAlertHost
import com.example.bdui.ui.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val appThemeViewModel: AppThemeViewModel = koinViewModel()
            val selectedTheme by appThemeViewModel.currentTheme.collectAsState()

            AppTheme(
                selectedTheme = selectedTheme
            ) {
                val navController = rememberNavController()

                var activeChrome by remember {
                    mutableStateOf<Triple<ScreenPage, ScreenConfig, BduiScreenContext>?>(null)
                }

                val currentBackStackEntry by navController.currentBackStackEntryAsState()

                val activePage = remember(currentBackStackEntry) {
                    runCatching {
                        currentBackStackEntry?.toRoute<ScreenPage>()
                    }.getOrNull()
                }

                val bottomBarState = remember {
                    BottomBarState()
                }

                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalBottomBarState provides bottomBarState
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        val chrome = activeChrome

                        val screenConfig = chrome?.second
                        val renderContext = chrome?.third

                        AppScaffold(
                            topBar = {
                                val appBar = screenConfig?.appBar
                                if (appBar != null && renderContext != null) {
                                    BduiAppBarRegistry.render(
                                        node = appBar,
                                        context = renderContext
                                    )
                                }
                            },
                            showBottomBar = screenConfig?.bottomBar != null,
                            bottomBar = {
                                val bottomBar = screenConfig?.bottomBar
                                if (bottomBar != null && renderContext != null) {
                                    BduiBottomBarRegistry.render(
                                        node = bottomBar,
                                        context = renderContext
                                    )
                                }
                            },
                            screenOrientation = screenConfig?.screenOrientation ?: "portrait",
                            statusBarVisible = screenConfig?.statusBarVisible ?: true,
                            navigationBarVisible = screenConfig?.navigationBarVisible ?: true
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = ScreenPage(screenId = "home"),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                animatedComposable<ScreenPage>(
                                    navController = navController
                                ) { page ->
                                    BduiScreenHost(page = page) { config, route ->
                                        BduiScreenContent(
                                            config = config,
                                            page = route,
                                            navController = navController,
                                            onScreenContextReady = { newConfig, newPage, context ->
                                                // Во время анимации не даём старому экрану
                                                // перезаписать панели активного маршрута
                                                if (
                                                    newPage.screenId == activePage?.screenId &&
                                                    newPage.argsJson == activePage.argsJson
                                                ) {
                                                    activeChrome = Triple(
                                                        newPage,
                                                        newConfig,
                                                        context
                                                    )
                                                }
                                            }
                                        )
                                    }
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