package com.example.bdui.ui.bdui.renderer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.bdui.ui.items.CustomOutlinedTextField
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object BduiElementRegistry {

    val renderers: Map<String, BduiComponentRenderer> = mapOf(
        "text" to { node, context, modifier ->
            val value = node["text"]
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()

            // Подстановка значений состояния — ответственность контекста.
            Text(
                text = context.resolve(value),
                modifier = modifier.fillMaxWidth()
            )
        },

        "button" to { node, context, modifier ->
            val text = node["text"]
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()

            Button(
                onClick = {
                    val action = node["action"]?.jsonObject

                    if (action != null) {
                        context.actionHandler.executeNavigationAction(action)
                    }
                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text)
            }
        },

        "textField" to { node, context, modifier ->
            // stateKey обязателен: по нему хранится введённое пользователем значение.
            val key = node["stateKey"]
                ?.jsonPrimitive
                ?.contentOrNull

            val label = node["label"]
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()

            if (key.isNullOrBlank()) {
                // Некорректная конфигурация не должна обрушать весь экран.
                // В режиме разработки выводим причину прямо в UI.
                Text(
                    text = "Ошибка BDUI: у textField отсутствует stateKey",
                    modifier = modifier.fillMaxWidth()
                )
            } else {
                // Значение берётся из общего состояния экрана.
                // Если пользователь ещё ничего не ввёл, поле будет пустым.
                CustomOutlinedTextField(
                    value = context.values[key].orEmpty(),
                    label = context.resolve(label),
                    singleLine = true,
                    onValueChange = { newValue ->
                        // SnapshotStateMap уведомит Compose об изменении значения.
                        context.values[key] = newValue
                    },
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    )
}
