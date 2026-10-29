package com.example.bdui.ui.bdui.parser

import com.example.bdui.ui.bdui.model.ScreenConfig

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Загружает JSON-конфигурации BDUI из assets/bdui/ (позже через API)
 *
 * Loader не отображает UI и не управляет навигацией
 * Его задача — вернуть проверенную модель ScreenConfig
 */
class BduiConfigLoader(
    context: Context
) {
    // Храним applicationContext, чтобы Loader случайно не удерживал Activity.
    private val appContext = context.applicationContext

    // Настройки JSON используются для всех конфигураций одинаково.
    private val json = Json {
        // Позволяет добавлять необязательные поля в новые версии JSON,
        // не ломая старые клиенты, если эти поля не нужны им.
        ignoreUnknownKeys = true

        // Не разрешаем нестрогий синтаксис JSON.
        isLenient = false
    }

    /**
     * Загружает экран по идентификатору.
     *
     * Для screenId = "home" читает assets/bdui/home.json.
     */
    fun load(screenId: String): ScreenConfig {
        // Не разрешаем использовать произвольные пути вместо ID экрана.
        // Это защищает от попыток выйти за пределы каталога bdui.
        require(
            screenId.matches(Regex("[a-zA-Z0-9_-]+"))
        ) {
            "Invalid BDUI screenId: $screenId"
        }

        // Читаем файл и закрываем поток автоматически через use.
        val source = appContext.assets
            .open("bdui/$screenId.json")
            .bufferedReader()
            .use { reader -> reader.readText() }

        // Сначала разбираем JSON в универсальную структуру.
        // Ошибка синтаксиса приведёт к исключению на этом этапе.
        val root = json.parseToJsonElement(source) as? JsonObject
            ?: throw BduiConfigException(
                "BDUI config '$screenId' must be a JSON object"
            )

        // Проверяем структуру ДО создания модели и отображения UI.
        BduiConfigValidator.validate(root)

        // После проверки преобразуем JSON в типизированную модель.
        val config = json.decodeFromJsonElement<ScreenConfig>(root)

        // Проверяем, что файл соответствует запрошенному экрану.
        // Например, home.json не должен случайно содержать screenId=settings.
        if (config.screenId != screenId) {
            throw BduiConfigException(
                "Requested '$screenId', but config declares '${config.screenId}'"
            )
        }

        return config
    }
}