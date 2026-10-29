package com.example.bdui.ui.bdui.actions

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import com.example.bdui.ui.bdui.model.ScreenPage
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
        navigateToScreen(destination, args)
    }

    fun executeTabNavigationAction(action: JsonObject) {
        val type = action["type"]
            ?.jsonPrimitive
            ?.contentOrNull

        if (type != "navigate") return

        val destination = action["destination"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return

        val argsObject = action["args"]
            ?.jsonObject
            .orEmpty()

        val args = argsObject.mapValues { (_, value) ->
            val raw = value.jsonPrimitive.contentOrNull.orEmpty()

            if (raw.startsWith("$")) {
                values[raw.drop(1)].orEmpty()
            } else {
                raw
            }
        }

        navigateToScreen(destination, args)
    }

    private fun navigateToScreen(
        destination: String,
        args: Map<String, String>
    ) {
        val existingEntry = navController.currentBackStack.value
            .lastOrNull { entry ->
                runCatching {
                    entry.toRoute<ScreenPage>().screenId == destination
                }.getOrDefault(false)
            }

        if (existingEntry != null) {
            navController.popBackStack(
                route = existingEntry.toRoute<ScreenPage>(),
                inclusive = false
            )
        } else {
            navController.navigate(createBduiRoute(destination, args))
        }
    }
}