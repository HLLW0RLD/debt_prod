package com.example.bdui.ui.bdui.renderer.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bdui.ui.bdui.actions.BduiActionHandler
import com.example.bdui.ui.bdui.model.ScreenConfig
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.model.readArgs
import com.example.bdui.ui.bdui.renderer.chrome.BduiAppBarRegistry
import com.example.bdui.ui.bdui.renderer.chrome.BduiBottomBarRegistry
import com.example.bdui.ui.theme.AppScaffold
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Собирает полноценный Compose-экран из BDUI-конфигурации.
 *
 * BduiScreenHost отвечает за загрузку JSON, а этот компонент —
 * за отображение его содержимого и обработку пользовательских действий.
 *
 * Экран не зависит от конкретной предметной области: его содержимое
 * и доступные действия определяются JSON-конфигурацией.
 */
@Composable
fun BduiScreenContent(
    config: ScreenConfig,
    page: ScreenPage,
    navController: NavController,
    modifier: Modifier = Modifier,
    onScreenContextReady: (
        config: ScreenConfig,
        page: ScreenPage,
        context: BduiScreenContext
    ) -> Unit = { _, _, _ -> }
) {
    // Состояние полей хранится в Compose и обновляется при вводе
    // При переходе на другой screenId для нового экрана создаётся своё состояние
    val values = remember(page.screenId) {
        mutableStateMapOf<String, String>()
    }

    // Аргументы перехода доступны компонентам через шаблоны вида {{name}}
    val routeArgs = remember(page.argsJson) {
        page.readArgs()
    }

    // Обработчик отделяет действия из JSON от визуальных компонентов
    // Он использует тот же NavController, что и основной NavHost
    val actionHandler = remember(navController, values) {
        BduiActionHandler(
            navController = navController,
            values = values
        )
    }

    val screenTitle = (
            config.appBar?.get("title") as? JsonPrimitive
            )?.contentOrNull
        ?.takeIf { it.isNotBlank() }
        ?: config.title

    // Контекст передаётся всем вложенным элементам
    // Благодаря этому поля, подстановки и действия используют общее состояние
    val renderContext = remember(
        navController,
        values,
        actionHandler,
        routeArgs
    ) {
        BduiScreenContext(
            navController = navController,
            title = screenTitle,
            values = values,
            actionHandler = actionHandler,
            args = routeArgs
        )
    }

    SideEffect {
        onScreenContextReady(config, page, renderContext)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BduiRenderer(
            node = config.body,
            context = renderContext,
            modifier = Modifier.fillMaxSize()
        )
    }
}