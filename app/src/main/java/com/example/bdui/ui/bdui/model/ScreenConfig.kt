package com.example.bdui.ui.bdui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Типизированная конфигурация BDUI-экрана.
 *
 * schemaVersion определяет версию формата JSON-конфигурации (не версия приложения)
 *
 * screenId идентифицирует экран.
 * Например: home, details, settings
 * По нему Loader находит файл home.json и т. п.
 *
 * title содержит заголовок экрана, который Host отображает над содержимым
 *
 * body описывает JSON-дерево компонентов основного содержимого
 *
 * bottomBar содержит необязательную конфигурацию нижней панели
 * Если null, Host не создаёт нижнюю панель из BDUI-конфига
 */
@Serializable
data class ScreenConfig(
    val schemaVersion: Int,
    val screenId: String,
    val title: String,
    val body: JsonObject,

    // Необязательные конфигурации панелей
    val appBar: JsonObject? = null,
    val bottomBar: JsonObject? = null,

    // Настройки экрана
    val screenOrientation: String = "portrait",
    val statusBarVisible: Boolean = true, // Управляет отображением статус-бара
    val navigationBarVisible: Boolean = true, // Управляет отображением системной навигационной панели

    // null - наследовать тему приложения
    // можно передать тему если предполагается конкретная
    val theme: String? = null
)