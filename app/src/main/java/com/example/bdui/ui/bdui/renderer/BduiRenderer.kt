package com.example.bdui.ui.bdui.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun BduiRenderer(
    node: JsonObject,
    context: BduiRenderContext,
    modifier: Modifier = Modifier
) {
    // 1. Определяем тип текущего узла.
    val type = node["type"]
        ?.jsonPrimitive
        ?.contentOrNull
        ?: return

    // 2. Сначала ищем контейнер.
    val containerRenderer = BduiContainerRegistry.renderers[type]

    // 3. Если это не контейнер, ищем обычный элемент.
    val elementRenderer = BduiElementRegistry.renderers[type]

    when {
        containerRenderer != null -> {
            // Контейнер сам отвечает за расположение детей.
            containerRenderer(node, context, modifier)
        }

        elementRenderer != null -> {
            // Обычный элемент отображается без обхода children.
            elementRenderer(node, context, modifier)
        }

        else -> {
            // Неизвестный тип не должен обрушать всё приложение.
            Text(
                text = "Неизвестный компонент: $type",
                modifier = modifier
            )
        }
    }
}