package com.example.bdui.ui.bdui.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/**
 * Ошибка конфигурации BDUI.
 *
 * Сообщение содержит путь до проблемного поля, чтобы было проще
 * исправить JSON, например:
 * body.children[1].type
 */
class BduiConfigException(
    message: String
) : IllegalArgumentException(message)

/**
 * Проверяет структуру BDUI JSON перед преобразованием в ScreenConfig.
 *
 * Не зависит от Compose и NavController.
 */
object BduiConfigValidator {

    private const val SUPPORTED_SCHEMA_VERSION = 1

    private val supportedOrientations = setOf(
        "portrait",
        "landscape",
        "unspecified"
    )

    private val supportedThemes = setOf(
        "light",
        "dark"
    )

    fun validate(root: JsonObject) {
        val version = root["schemaVersion"].asInt(
            path = "schemaVersion"
        )

        if (version != SUPPORTED_SCHEMA_VERSION) {
            throw BduiConfigException(
                "Unsupported schemaVersion=$version. " +
                        "Supported version: $SUPPORTED_SCHEMA_VERSION"
            )
        }

        root["screenId"].asNonBlankString("screenId")
        root["title"].asString("title")

        // Основное содержимое обязательно
        val body = root["body"].asObject("body")
        validateNode(body, "body")

        // Верхняя и нижняя панели необязательны
        validateAppBar(root)
        validateBottomBar(root)

        // Ориентация экрана
        root["screenOrientation"]?.let { element ->
            val orientation = element.asString("screenOrientation")

            if (orientation !in supportedOrientations) {
                throw BduiConfigException(
                    "screenOrientation must be one of: " +
                            supportedOrientations.joinToString()
                )
            }
        }

        // Видимость системных панелей
        root["statusBarVisible"]?.asBoolean("statusBarVisible")
        root["navigationBarVisible"]?.asBoolean("navigationBarVisible")

        // null означает наследование глобальной темы приложения
        root["theme"]?.let { element ->
            if (element != JsonNull) {
                val theme = element.asString("theme")

                if (theme !in supportedThemes) {
                    throw BduiConfigException(
                        "theme must be null, 'light' or 'dark'"
                    )
                }
            }
        }
    }

    private fun validateOptionalNode(
        root: JsonObject,
        key: String
    ) {
        root[key]?.let { element ->
            if (element == JsonNull) return

            validateNode(
                node = element.asObject(key),
                path = key
            )
        }
    }

    private fun resolveAppBarType(appBar: JsonObject): String {
        val element = appBar["type"] ?: return "default"

        if (element == JsonNull) return "default"

        return element
            .asString("appBar.type")
            .trim()
            .ifEmpty { "default" }
    }

    private fun validateAppBar(root: JsonObject) {
        val element = root["appBar"] ?: return
        if (element == JsonNull) return

        val appBar = element.asObject("appBar")

        val type = resolveAppBarType(appBar)

        if (type !in setOf("default", "search")) {
            throw BduiConfigException(
                "appBar.type must be 'default' or 'search'"
            )
        }

        appBar["showBackButton"]?.asBoolean("appBar.showBackButton")
        appBar["showDivider"]?.asBoolean("appBar.showDivider")
        appBar["isCentered"]?.asBoolean("appBar.isCentered")

        appBar["height"]?.let {
            if (it != JsonNull) {
                val height = (it as? JsonPrimitive)?.contentOrNull?.toFloatOrNull()
                    ?: throw BduiConfigException(
                        "appBar.height must be a number or numeric string"
                    )

                if (height <= 0f) {
                    throw BduiConfigException(
                        "appBar.height must be greater than 0"
                    )
                }
            }
        }

        appBar["textSize"]?.let {
            if (it != JsonNull) {
                val size = (it as? JsonPrimitive)?.contentOrNull?.toFloatOrNull()
                    ?: throw BduiConfigException(
                        "appBar.textSize must be a number or numeric string"
                    )

                if (size <= 0f) {
                    throw BduiConfigException(
                        "appBar.textSize must be greater than 0"
                    )
                }
            }
        }

        when (type) {
            "default" -> {
                appBar["title"]?.asString("appBar.title")

            }

            "search" -> {
                appBar["title"]?.asString("appBar.title")
                appBar["placeholder"]?.asNonBlankString(
                    "appBar.placeholder"
                )
                appBar["valueKey"]?.asNonBlankString(
                    "appBar.valueKey"
                )
            }
        }

        appBar["optionsMode"]?.let { modeElement ->
            val mode = modeElement.asString("appBar.optionsMode")
            if (mode !in setOf("menu", "button")) {
                throw BduiConfigException("appBar.optionsMode must be 'menu' or 'button'")
            }
        }
        appBar["options"]?.let { optionsElement ->
            if (optionsElement != JsonNull) {
                val options = optionsElement as? JsonArray
                    ?: throw BduiConfigException("appBar.options must be a JSON array")
                options.forEachIndexed { index, optionElement ->
                    val optionPath = "appBar.options[$index]"
                    val option = optionElement.asObject(optionPath)
                    option["label"].asNonBlankString("$optionPath.label")
                    option["action"]?.let { actionElement ->
                        val action = actionElement.asObject("$optionPath.action")
                        action["type"].asNonBlankString("$optionPath.action.type")
                    } ?: throw BduiConfigException("$optionPath.action is required")
                }
            }
        }
        if (appBar["action"] != null && appBar["action"] != JsonNull) {
            val action = appBar["action"].asObject("appBar.action")
            action["type"].asNonBlankString("appBar.action.type")
        }
    }

    private fun validateBottomBar(root: JsonObject) {
        val element = root["bottomBar"] ?: return
        if (element == JsonNull) return

        val bottomBar = element.asObject("bottomBar")

        val itemsElement = bottomBar["items"]
            ?: throw BduiConfigException(
                "bottomBar.items is required"
            )

        val items = itemsElement as? JsonArray
            ?: throw BduiConfigException(
                "bottomBar.items must be a JSON array"
            )

        if (items.isEmpty()) {
            throw BduiConfigException(
                "bottomBar.items must not be empty"
            )
        }

        val ids = mutableSetOf<String>()

        items.forEachIndexed { index, itemElement ->
            val path = "bottomBar.items[$index]"
            val item = itemElement.asObject(path)

            val id = item["id"].asNonBlankString("$path.id")
            item["label"].asNonBlankString("$path.label")

            if (!ids.add(id)) {
                throw BduiConfigException(
                    "$path.id duplicates id '$id'"
                )
            }

            item["action"]?.let { actionElement ->
                if (actionElement != JsonNull) {
                    val action = actionElement.asObject("$path.action")
                    action["type"].asNonBlankString("$path.action.type")
                }
            }
        }

        bottomBar["selectedItem"]?.let { selectedElement ->
            val selected = selectedElement.asNonBlankString(
                "bottomBar.selectedItem"
            )

            if (selected !in ids) {
                throw BduiConfigException(
                    "bottomBar.selectedItem must match an item id"
                )
            }
        }
    }

    /**
     * Рекурсивная проверка узлов дерева.
     */
    private fun validateNode(
        node: JsonObject,
        path: String
    ) {
        val type = node["type"].asNonBlankString("$path.type")

        // Проверяем обязательные поля целевого контракта BDUI до рендера.
        when (type) {
            "text_view_default", "text" -> node["text"].asString("$path.text")
            "button_text_icon_default" -> {
                node["text"].asString("$path.text")
                node["icon"].asNonBlankString("$path.icon")
            }
            "button_text_default", "button_text_ghost", "button" ->
                node["text"].asString("$path.text")
            "button_icon_default", "button_icon_ghost", "button_floating_action" ->
                node["icon"].asNonBlankString("$path.icon")
            "field_input_outlined", "field_input_password", "field_input_date", "field_input_time", "textField" ->
                node["stateKey"].asNonBlankString("$path.stateKey")
            "selection_radio_default" -> {
                node["stateKey"].asNonBlankString("$path.stateKey")
                node["value"].asNonBlankString("$path.value")
                node["text"].asString("$path.text")
            }
            "selection_switch_default", "selection_checkbox_default" ->
                node["stateKey"].asNonBlankString("$path.stateKey")
            "field_input_dropdown" -> {
                node["stateKey"].asNonBlankString("$path.stateKey")
                val options = node["options"] as? JsonArray
                    ?: throw BduiConfigException("$path.options must be a JSON array")
                options.forEachIndexed { index, element ->
                    val optionPath = "$path.options[$index]"
                    val option = element.asObject(optionPath)
                    option["value"].asNonBlankString("$optionPath.value")
                    option["label"].asNonBlankString("$optionPath.label")
                }
            }
        }

        node["enabled"]?.asBoolean("$path.enabled")
        node["visible"]?.asBoolean("$path.visible")
        node["children"]?.let { childrenElement ->
            if (childrenElement == JsonNull) return@let

            val children = childrenElement as? JsonArray
                ?: throw BduiConfigException(
                    "$path.children must be a JSON array"
                )

            children.forEachIndexed { index, child ->
                val childPath = "$path.children[$index]"

                validateNode(
                    node = child.asObject(childPath),
                    path = childPath
                )
            }
        }

        node["action"]?.let { actionElement ->
            if (actionElement == JsonNull) return@let

            val action = actionElement.asObject("$path.action")
            action["type"].asNonBlankString("$path.action.type")
        }
    }

    /**
     * Проверяет, что значение существует и является непустой строкой.
     */
    private fun JsonElement?.asNonBlankString(
        path: String
    ): String {
        val value = asString(path)

        if (value.isBlank()) {
            throw BduiConfigException("$path must not be blank")
        }

        return value
    }

    /**
     * Проверяет строковое значение JSON.
     */
    private fun JsonElement?.asString(
        path: String
    ): String {
        val primitive = this as? JsonPrimitive
            ?: throw BduiConfigException("$path must be a string")

        if (!primitive.isString) {
            throw BduiConfigException("$path must be a string")
        }

        return primitive.contentOrNull
            ?: throw BduiConfigException("$path must not be null")
    }

    /**
     * Проверяет целочисленное значение JSON.
     */
    private fun JsonElement?.asInt(
        path: String
    ): Int {
        val primitive = this as? JsonPrimitive
            ?: throw BduiConfigException("$path must be an integer")

        return primitive.content.toIntOrNull()
            ?: throw BduiConfigException("$path must be an integer")
    }

    private fun JsonElement?.asBoolean(
        path: String
    ): Boolean {
        val primitive = this as? JsonPrimitive
            ?: throw BduiConfigException("$path must be a boolean")

        return primitive.content
            .takeIf { primitive.booleanOrNull != null }
            ?.toBooleanStrict()
            ?: throw BduiConfigException("$path must be a boolean")
    }

    private fun JsonElement?.asObject(
        path: String
    ): JsonObject {
        return this as? JsonObject
            ?: throw BduiConfigException("$path must be a JSON object")
    }
}