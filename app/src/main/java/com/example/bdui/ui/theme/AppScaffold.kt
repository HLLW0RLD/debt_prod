package com.example.bdui.ui.theme

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bdui.ui.utils.findActivity

/**
 * Общая оболочка экрана BDUI.
 *
 * Управляет верхней и нижней панелями, ориентацией устройства
 * и видимостью системных панелей Android.
 *
 */
@Composable
fun AppScaffold(
    topBar: @Composable () -> Unit = {},
    showBottomBar: Boolean = false,
    bottomBar: (@Composable () -> Unit)? = null,
    screenOrientation: String = "portrait",
    statusBarVisible: Boolean = true,
    navigationBarVisible: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()
    val bottomBarState = LocalBottomBarState.current

    // Сохраняем возможность глобально управлять нижней панелью
    SideEffect {
        bottomBarState.visible = showBottomBar
    }

    // Преобразуем читаемое значение из JSON в константу Android
    val orientation = when (screenOrientation.lowercase()) {
        "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        "unspecified" -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Меняем ориентацию при изменении настройки экрана
    LaunchedEffect(orientation) {
        context.findActivity().requestedOrientation = orientation
    }

    // TODO не проверено
    // Управляем видимостью системных панелей отдельно от AppBar
    LaunchedEffect(view, statusBarVisible, navigationBarVisible) {
        val controller = WindowInsetsControllerCompat(
            activity.window,
            view
        )

        if (statusBarVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }

        if (navigationBarVisible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }

        // При скрытых системных панелях разрешаем временное
        // появление по системному жесту пользователя
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    Scaffold(
        topBar = topBar,
        bottomBar = {
            if (
                bottomBarState.enabled &&
                bottomBarState.visible &&
                bottomBar != null
            ) {
                bottomBar()
            }
        },
        content = { innerPadding ->
            content(innerPadding)
        }
    )
}