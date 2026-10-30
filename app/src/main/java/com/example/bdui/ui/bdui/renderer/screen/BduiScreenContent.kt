
package com.example.bdui.ui.bdui.renderer.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.bdui.domain.local.BduiStorage
import com.example.bdui.ui.bdui.actions.BduiActionHandler
import com.example.bdui.ui.bdui.actions.BduiErrorHandler
import com.example.bdui.ui.bdui.actions.BduiStorageActionHandler
import com.example.bdui.ui.bdui.model.ScreenConfig
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.model.readArgs
import com.example.bdui.ui.theme.AppScaffold
import com.example.bdui.ui.utils.AlertManager
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.koin.compose.koinInject

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
    // Общее состояние полей текущего экрана
    val values = remember(page.screenId, page.argsJson) {
        mutableStateMapOf<String, String>().apply {
            putAll(page.readArgs())
        }
    }

    // Структурированные данные: JSON-объекты, массивы и примитивы
    val data = remember(page.screenId) {
        mutableStateMapOf<String, JsonElement>()
    }

    // Аргументы перехода доступны через шаблоны вида {{name}}
    val routeArgs = remember(page.argsJson) {
        page.readArgs()
    }

    // Зависимости хранилища и обработки ошибок
    val storage: BduiStorage = koinInject()
    val alertManager: AlertManager = koinInject()

    val errorHandler = remember(alertManager) {
        BduiErrorHandler(alertManager)
    }

    val scope = rememberCoroutineScope()

    // Обработчик навигационных действий
    val actionHandler = remember(navController, values) {
        BduiActionHandler(
            navController = navController,
            values = values
        )
    }

    // Обработчик действий хранилища
    val storageActionHandler = remember(
        storage,
        values,
        data,
        scope,
        errorHandler
    ) {
        BduiStorageActionHandler(
            storage = storage,
            values = values,
            data = data,
            scope = scope,
            errorHandler = errorHandler
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        page.screenId,
        routeArgs,
        storageActionHandler
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_RESUME &&
                page.screenId == "debt_details"
            ) {
                val id = routeArgs["id"]

                if (!id.isNullOrBlank()) {
                    storageActionHandler.executeStorageAction(
                        JsonObject(
                            mapOf(
                                "type" to JsonPrimitive("load_item"),
                                "key" to JsonPrimitive("records"),
                                "stateKey" to JsonPrimitive("record"),
                                "id" to JsonPrimitive(id)
                            )
                        )
                    )
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(page.screenId, storageActionHandler) {
        if (page.screenId == "debts") {
            storageActionHandler.initializeArray(
                key = "records",
                stateKey = "records"
            )
        }
    }

    val screenTitle = (
            config.appBar?.get("title") as? JsonPrimitive
            )?.contentOrNull
        ?.takeIf { it.isNotBlank() }
        ?: config.title

    // Общий контекст передаётся всем вложенным элементам
    val renderContext = remember(
        navController,
        values,
        data,
        actionHandler,
        storageActionHandler,
        routeArgs,
        screenTitle
    ) {
        BduiScreenContext(
            navController = navController,
            title = screenTitle,
            values = values,
            data = data,
            actionHandler = actionHandler,
            storageActionHandler = storageActionHandler,
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