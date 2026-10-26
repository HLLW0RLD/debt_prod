package com.example.bdui.ui.bdui.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
 * Проверяет структуру BDUI JSON перед преобразованием в BduiConfig.
 *
 * Здесь нет Compose-кода и нет зависимости от NavController.
 * Поэтому валидатор можно тестировать обычными unit-тестами.
 */
object BduiConfigValidator {

    // Пока приложение поддерживает только эту версию формата.
    private const val SUPPORTED_SCHEMA_VERSION = 1

    /**
     * Основная точка входа.
     *
     * Loader вызывает validate(root), а только после успешной проверки
     * преобразует JSON в BduiConfig.
     */
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

        // screenId и title нужны Host для выбора и оформления экрана.
        root["screenId"].asNonBlankString("screenId")
        root["title"].asString("title")

        // body обязателен, поскольку это основное содержимое экрана.
        val body = root["body"].asObject("body")
        validateNode(body, "body")

        // Нижняя панель необязательна, но если указана,
        // она тоже должна иметь корректную структуру узла.
        root["bottomBar"]?.let { bottomBar ->
            validateNode(
                node = bottomBar.asObject("bottomBar"),
                path = "bottomBar"
            )
        }
    }

    /**
     * Рекурсивно проверяет узел и всех его потомков.
     *
     * Метод не перечисляет column, row, text и другие типы.
     * Он проверяет общую структуру узла независимо от того,
     * какой renderer впоследствии обработает этот type.
     */
    private fun validateNode(
        node: JsonObject,
        path: String
    ) {
        // У каждого узла должен быть строковый type.
        node["type"].asNonBlankString("$path.type")

        // children, если присутствует, должен быть массивом объектов.
        // Каждый ребёнок проходит ту же рекурсивную проверку.
        node["children"]?.let { childrenElement ->
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

        // action, если указано, должен быть объектом с непустым type.
        node["action"]?.let { actionElement ->
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

    /**
     * Проверяет, что значение является JSON-объектом.
     */
    private fun JsonElement?.asObject(
        path: String
    ): JsonObject {
        return this as? JsonObject
            ?: throw BduiConfigException("$path must be a JSON object")
    }
}