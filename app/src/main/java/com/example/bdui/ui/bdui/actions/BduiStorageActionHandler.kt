
package com.example.bdui.ui.bdui.actions

import com.example.bdui.domain.local.BduiStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class BduiStorageActionHandler(
    private val storage: BduiStorage,
    private val values: MutableMap<String, String>,
    private val data: MutableMap<String, JsonElement>,
    private val scope: CoroutineScope,
    private val errorHandler: BduiErrorHandler
) {

    fun initializeArray(key: String, stateKey: String) {
        scope.launch {
            try {
                try {
                    // Сначала пробуем загрузить существующую коллекцию
                    data[stateKey] = storage.load(key)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    // Если коллекции нет, создаём её
                    storage.create(key, JsonArray(emptyList()))
                    data[stateKey] = storage.load(key)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorHandler.handle(error)
            }
        }
    }

    fun executeStorageAction(action: JsonObject) {
        scope.launch {
            try {
                when (action.string("type")) {
                    "create_value" -> createValue(action)
                    "load_value" -> loadValue(action)
                    "update_value" -> updateValue(action)
                    "delete_value" -> deleteValue(action)
                    "clear_storage" -> storage.clear()

                    "load_data" -> loadData(action)
                    "load_item" -> loadItem(action)
                    "append_item" -> appendItem(action)
                    "update_item" -> updateItem(action)
                    "delete_item" -> deleteItem(action)

                    else -> throw IllegalArgumentException(
                        "Unknown storage action: ${action.string("type")}"
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorHandler.handle(error)
            }
        }
    }

    // Создаёт новую запись в хранилище
    private suspend fun createValue(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val value = resolveElement(action.requiredValue("value"))

        storage.create(key, value)
    }

    // Загружает примитивное значение в строковое состояние values
    private suspend fun loadValue(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val stateKey = resolveString(action.requiredString("stateKey"))

        val result = storage.load(key)

        values[stateKey] = when (result) {
            is JsonPrimitive -> result.content
            else -> result.toString()
        }
    }

    // Обновляет существующую запись
    private suspend fun updateValue(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val value = resolveElement(action.requiredValue("value"))

        storage.update(key, value)
    }

    // Удаляет запись
    private suspend fun deleteValue(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))

        storage.delete(key)
    }

    // Загружает полноценный JSON в структурированное состояние data
    private suspend fun loadData(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val stateKey = resolveString(action.requiredString("stateKey"))

        data[stateKey] = storage.load(key)
    }

    // Загружает один объект из JSON-массива по ID
    private suspend fun loadItem(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val stateKey = resolveString(action.requiredString("stateKey"))
        val idField = action.string("idField") ?: "id"
        val id = resolveElement(action.requiredValue("id"))

        val current = storage.load(key) as? JsonArray
            ?: throw IllegalArgumentException(
                "Storage value '$key' is not a JSON array"
            )

        val item = current.firstOrNull { element ->
            val obj = element as? JsonObject
            obj?.get(idField) == id
        } ?: throw IllegalArgumentException(
            "Item with ID '${id.toString()}' was not found"
        )

        data[stateKey] = item
    }

    // Добавляет объект в JSON-массив
    private suspend fun appendItem(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val item = resolveElement(action.requiredValue("item"))

        val current = storage.load(key) as? JsonArray
            ?: throw IllegalArgumentException(
                "Storage value '$key' is not a JSON array"
            )

        val insertPosition = action.string("insertPosition") ?: "start"

        val updatedArray = when (insertPosition) {
            "start" -> JsonArray(listOf(item) + current)
            "end" -> JsonArray(current + item)
            else -> throw IllegalArgumentException(
                "Unknown insertPosition: $insertPosition"
            )
        }

        storage.update(key, updatedArray)
        syncData(key, action.string("stateKey"))
    }

    // Обновляет объект внутри массива по значению ID
    private suspend fun updateItem(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val idField = action.string("idField") ?: "id"
        val id = resolveElement(action.requiredValue("id"))
        val updatedItem = resolveElement(
            action.requiredValue("item")
        ) as? JsonObject
            ?: throw IllegalArgumentException(
                "Parameter 'item' must be a JSON object"
            )

        val current = storage.load(key) as? JsonArray
            ?: throw IllegalArgumentException(
                "Storage value '$key' is not a JSON array"
            )

        var found = false

        val updatedArray = current.map { element ->
            val item = element as? JsonObject

            if (
                item != null &&
                item[idField] == id
            ) {
                found = true

                // Объединяем старые поля с новыми
                JsonObject(item + updatedItem)
            } else {
                element
            }
        }

        if (!found) {
            throw IllegalArgumentException(
                "Item with ID '${id.toString()}' was not found"
            )
        }

        storage.update(key, JsonArray(updatedArray))
        syncData(key, action.string("stateKey"))
    }

    // Удаляет объект из массива по значению ID
    private suspend fun deleteItem(action: JsonObject) {
        val key = resolveString(action.requiredString("key"))
        val idField = action.string("idField") ?: "id"
        val id = resolveElement(action.requiredValue("id"))

        val current = storage.load(key) as? JsonArray
            ?: throw IllegalArgumentException(
                "Storage value '$key' is not a JSON array"
            )

        val updatedArray = current.filterNot { element ->
            val item = element as? JsonObject
            item?.get(idField) == id
        }

        if (updatedArray.size == current.size) {
            throw IllegalArgumentException(
                "Item with ID '${id.toString()}' was not found"
            )
        }

        storage.update(key, JsonArray(updatedArray))
        syncData(key, action.string("stateKey"))
    }

    // Рекурсивно обрабатывает ссылки на значения вида "$field"
    private fun resolveElement(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(
                element.mapValues { (_, value) ->
                    resolveElement(value)
                }
            )

            is JsonArray -> JsonArray(
                element.map { value ->
                    resolveElement(value)
                }
            )

            is JsonPrimitive -> {
                if (element.isString && element.content.startsWith("$")) {
                    JsonPrimitive(resolveString(element.content))
                } else {
                    element
                }
            }

            JsonNull -> JsonNull
        }
    }

    private fun resolveString(value: String): String {
        if (!value.startsWith("$")) return value

        return values[value.drop(1)].orEmpty()
    }

    private suspend fun syncData(key: String, stateKey: String?) {
        if (!stateKey.isNullOrBlank()) {
            data[stateKey] = storage.load(key)
        }
    }

    private fun JsonObject.string(key: String): String? {
        return this[key]
            ?.jsonPrimitive
            ?.contentOrNull
    }

    private fun JsonObject.requiredString(key: String): String {
        return string(key)
            ?: throw IllegalArgumentException(
                "Missing required parameter: $key"
            )
    }

    private fun JsonObject.requiredValue(key: String): JsonElement {
        return this[key]
            ?: throw IllegalArgumentException(
                "Missing required parameter: $key"
            )
    }
}