package com.example.bdui.ui.bdui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Типизированный маршрут для любого BDUI-экрана (модель для использования в навигации)
 *
 * screenId определяет, какой JSON нужно загрузить.
 * argsJson содержит аргументы конкретного перехода.
 *
 * Например:
 * screenId = "details"
 * argsJson = {"name":"Иван","email":"ivan@example.com"}
 */
@Serializable
data class ScreenPage(
    val screenId: String,

    // String используется, чтобы маршрут не зависел от поддержки
    // Map<String, String> в NavType текущей версии Navigation.
    // По умолчанию аргументов нет.

    val argsJson: String = "{}"
)

/**
 * Превращает обычную Map в маршрут, который понимает Navigation Compose.
 *
 * Вызывается в обработчике BDUI-действия перед navController.navigate().
 */
fun createBduiRoute(
    screenId: String,
    args: Map<String, String> = emptyMap()
): ScreenPage {
    return ScreenPage(
        screenId = screenId,
        argsJson = bduiJson.encodeToString(args)
    )
}

/**
 * Читает аргументы маршрута уже после перехода на экран.
 *
 * Это только аргументы перехода. Состояние редактируемых полей
 * приложения при необходимости хранится отдельно.
 */
fun ScreenPage.readArgs(): Map<String, String> {
    return bduiJson.decodeFromString(argsJson)
}

/**
 * Единый Json для сериализации аргументов маршрута.
 *
 * Если аргументы содержат неизвестные поля, при чтении они не вызовут
 * ошибку. При этом значения всё равно должны соответствовать типу String.
 */
private val bduiJson = Json {
    ignoreUnknownKeys = true
}