package com.example.bdui.ui.bdui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ScreenConfig(
    // Версия формата JSON, а не версия приложения.
    val schemaVersion: Int,

    // Идентификатор экрана. Например: home, details, settings.
    // По нему Loader находит файл home.json и т. п.
    val screenId: String,

    // Заголовок, который Host отображает над содержимым.
    val title: String,

    // Корневой узел дерева содержимого экрана.
    // Конкретный type (column, card и т. д.) определяет Renderer.
    val body: JsonObject,

    // Необязательное описание нижней панели.
    // Если null, Host не создаёт нижнюю панель из BDUI-конфига.
    val bottomBar: JsonObject? = null
)