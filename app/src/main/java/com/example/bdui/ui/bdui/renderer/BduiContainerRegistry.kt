package com.example.bdui.ui.bdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

typealias BduiComponentRenderer = @Composable (
    node: JsonObject,
    context: BduiRenderContext,
    modifier: Modifier
) -> Unit

object BduiContainerRegistry {

    val renderers: Map<String, BduiComponentRenderer> = mapOf(
        "column" to { node, context, modifier ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        (node["padding"]?.jsonPrimitive?.intOrNull ?: 0).dp
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    (node["spacing"]?.jsonPrimitive?.intOrNull ?: 12).dp
                )
            ) {
                // Важно: детей снова отображает общий renderer.
                // Поэтому внутри column могут быть row, card,
                // другие column и любые поддерживаемые элементы.
                BduiRenderChildren(
                    node = node,
                    context = context
                )
            }
        },

        "row" to { node, context, modifier ->
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    (node["spacing"]?.jsonPrimitive?.intOrNull ?: 8).dp
                )
            ) {
                BduiRenderChildren(
                    node = node,
                    context = context
                )
            }
        },

        "card" to { node, context, modifier ->
            Card(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BduiRenderChildren(
                        node = node,
                        context = context
                    )
                }
            }
        }
    )
}

@Composable
private fun BduiRenderChildren(
    node: JsonObject,
    context: BduiRenderContext
) {
    // Обходим всех детей по порядку.
    node["children"]
        ?.jsonArray
        ?.forEach { child ->
            // Рекурсивно вызываем общий renderer,
            // а не конкретный renderer column или row.
            BduiRenderer(
                node = child.jsonObject,
                context = context
            )
        }
}