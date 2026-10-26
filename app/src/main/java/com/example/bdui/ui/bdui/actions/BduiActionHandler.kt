package com.example.bdui.ui.bdui.actions

import androidx.navigation.NavController
import com.example.bdui.ui.bdui.model.createBduiRoute
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BduiActionHandler(
    private val navController: NavController,
    private val values: Map<String, String>
) {
    fun executeNavigationAction(action: JsonObject) {
        // Сначала читаем тип действия из JSON.
        val type = action["type"]
            ?.jsonPrimitive
            ?.contentOrNull

        // В этом обработчике пока поддерживаем только navigate.
        if (type != "navigate") return

        val destination = action["destination"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return

        // Получаем аргументы, которые указаны в конфигурации.
        val argsObject = action["args"]?.jsonObject.orEmpty()

        val args = argsObject.mapValues { (_, value) ->
            val raw = value.jsonPrimitive.contentOrNull.orEmpty()

            // "$name" означает ссылку на значение состояния.
            if (raw.startsWith("$")) {
                values[raw.drop(1)].orEmpty()
            } else {
                raw
            }
        }

        // Переход выполняется через тот же NavController,
        // который подключён к NavHost с animatedComposable.
        navController.navigate(
            createBduiRoute(destination, args)
        )
    }
}