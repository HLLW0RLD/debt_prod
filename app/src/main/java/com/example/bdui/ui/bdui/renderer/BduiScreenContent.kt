package com.example.bdui.ui.bdui.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bdui.ui.bdui.actions.BduiActionHandler
import com.example.bdui.ui.bdui.model.ScreenConfig
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.model.readArgs

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
    modifier: Modifier = Modifier
) {
    // Состояние полей хранится в Compose и обновляется при вводе.
    // При переходе на другой screenId для нового экрана создаётся своё состояние.
    val values = remember(page.screenId) {
        mutableStateMapOf<String, String>()
    }

    // Аргументы перехода доступны компонентам через шаблоны вида {{name}}.
    val routeArgs = remember(page.argsJson) {
        page.readArgs()
    }

    // Обработчик отделяет действия из JSON от визуальных компонентов.
    // Он использует тот же NavController, что и основной NavHost.
    val actionHandler = remember(navController, values) {
        BduiActionHandler(
            navController = navController,
            values = values
        )
    }

    // Контекст передаётся всем вложенным элементам.
    // Благодаря этому поля, подстановки и действия используют общее состояние.
    val renderContext = remember(
        navController,
        values,
        actionHandler,
        routeArgs
    ) {
        BduiRenderContext(
            navController = navController,
            values = values,
            actionHandler = actionHandler,
            args = routeArgs
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),

        // Нижняя панель необязательна. Если она задана в JSON,
        // её отображает тот же renderer, что и основное содержимое.
        bottomBar = {
            config.bottomBar?.let { bottomBar ->
                HorizontalDivider()
                BduiRenderer(
                    node = bottomBar,
                    context = renderContext,
                    modifier = Modifier
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Заголовок берётся из конфигурации, а не из названия приложения.
            Text(
                text = renderContext.resolve(config.title)
            )

            // Рендерер рекурсивно строит дерево из config.body:
            // column, row, card, text, button, textField и другие
            // компоненты, зарегистрированные в реестрах.
            BduiRenderer(
                node = config.body,
                context = renderContext,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}