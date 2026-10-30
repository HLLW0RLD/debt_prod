package com.example.bdui.ui.bdui.renderer.screen

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.navigation.NavController
import com.example.bdui.ui.bdui.actions.BduiActionHandler
import com.example.bdui.ui.bdui.actions.BduiStorageActionHandler
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class BduiScreenContext(
    val navController: NavController,

    // Необязательный заголовок текущего экрана
    val title: String? = null,

    // Общие значения полей формы
    val values: SnapshotStateMap<String, String>,

    // Структурированные данные
    val data: SnapshotStateMap<String, JsonElement>,

    // Обработчик навигации
    val actionHandler: BduiActionHandler,

    // Обработчик операций хранилища
    val storageActionHandler: BduiStorageActionHandler,

    // Аргументы текущего маршрута
    val args: Map<String, String>,

    // Текущий элемент repeat
    private val item: JsonElement? = null,

    // Индекс текущего элемента repeat
    private val itemIndex: Int? = null
) {

    fun executeAction(
        action: JsonObject,
        isTabNavigation: Boolean = false
    ) {
        when (action["type"]?.jsonPrimitive?.contentOrNull) {
            "navigate" -> {
                val argsObject = action["args"] as? JsonObject

                val resolvedArgs = argsObject?.mapValues { (_, value) ->
                    if (value is JsonPrimitive && value.isString) {
                        JsonPrimitive(resolve(value.content))
                    } else {
                        value
                    }
                }

                val resolvedAction = if (resolvedArgs != null) {
                    JsonObject(
                        action.toMutableMap().apply {
                            put("args", JsonObject(resolvedArgs))
                        }
                    )
                } else {
                    action
                }

                if (isTabNavigation) {
                    actionHandler.executeTabNavigationAction(resolvedAction)
                } else {
                    actionHandler.executeNavigationAction(resolvedAction)
                }
            }

            "create_value",
            "load_value",
            "update_value",
            "delete_value",
            "clear_storage",
            "load_data",
            "load_item",
            "append_item",
            "update_item",
            "delete_item" -> {
                storageActionHandler.executeStorageAction(action)
            }
        }
    }

    /**
     * Подставляет значения в шаблоны:
     * {{title}}
     * {{profile.name}}
     * {{item.name}}
     * {{itemIndex}}
     */
    fun resolve(template: String): String {
        val pattern = Regex("""\{\{([^}]+)\}\}""")

        return pattern.replace(template) { match ->
            val key = match.groupValues[1].trim()

            when {
                key == "title" -> title.orEmpty()

                key == "itemIndex" -> itemIndex?.toString().orEmpty()

                key == "item" -> item?.toDisplayString().orEmpty()

                key.startsWith("item.") -> {
                    getPath(item, key.removePrefix("item."))
                        ?.toDisplayString()
                        .orEmpty()
                }

                else -> {
                    getData(key)?.toDisplayString()
                        ?: values[key]
                        ?: args[key]
                        ?: ""
                }
            }
        }
    }

    /**
     * Получает значение из data по пути:
     * profile.name
     * debts
     * profile.settings.city
     */
    fun getData(path: String): JsonElement? {
        val parts = path.split(".")
        if (parts.isEmpty()) return null

        val root = data[parts.first()] ?: return null

        return getPath(root, parts.drop(1).joinToString("."))
    }

    /**
     * Создаёт контекст для конкретного элемента списка.
     * Остальные состояния и обработчики остаются общими.
     */
    fun withItem(
        item: JsonElement,
        index: Int
    ): BduiScreenContext {
        return BduiScreenContext(
            navController = navController,
            title = title,
            values = values,
            data = data,
            actionHandler = actionHandler,
            storageActionHandler = storageActionHandler,
            args = args,
            item = item,
            itemIndex = index
        )
    }

    private fun getPath(
        root: JsonElement?,
        path: String
    ): JsonElement? {
        if (root == null) return null
        if (path.isBlank()) return root

        var current: JsonElement = root

        for (part in path.split(".")) {
            current = when (current) {
                is JsonObject -> current[part] ?: return null

                is JsonArray -> {
                    val index = part.toIntOrNull() ?: return null
                    current.getOrNull(index) ?: return null
                }

                else -> return null
            }
        }

        return current
    }

    private fun JsonElement.toDisplayString(): String {
        return when (this) {
            JsonNull -> ""
            is JsonPrimitive -> contentOrNull.orEmpty()
            else -> toString()
        }
    }
}