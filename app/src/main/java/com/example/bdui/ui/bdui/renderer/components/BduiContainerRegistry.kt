package com.example.bdui.ui.bdui.renderer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import com.example.bdui.ui.bdui.renderer.screen.BduiRenderer
import com.example.bdui.ui.theme.AppColors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object BduiContainerRegistry {
    val renderers: Map<String, BduiComponentRenderer> = mapOf(
        "column" to { node, context, modifier ->
            val horizontal = when (node.string("horizontalAlignment", "start")) {
                "center" -> Alignment.CenterHorizontally
                "end" -> Alignment.End
                else -> Alignment.Start
            }

            Column(
                modifier = styledContainer(
                    node,
                    modifier,
                    node.color("backgroundColor", Color.Transparent)
                ),
                horizontalAlignment = horizontal,
                verticalArrangement = verticalArrangement(
                    node.string("verticalArrangement", "top"),
                    node.float("spacing", 12f).dp
                )
            ) {
                renderChildren(node, context)
            }
        },

        "row" to { node, context, modifier ->
            val vertical = when (node.string("verticalAlignment", "centerVertically")) {
                "top" -> Alignment.Top
                "bottom" -> Alignment.Bottom
                else -> Alignment.CenterVertically
            }

            Row(
                modifier = styledContainer(
                    node,
                    modifier,
                    node.color("backgroundColor", Color.Transparent)
                ),
                verticalAlignment = vertical,
                horizontalArrangement = horizontalArrangement(
                    node.string("horizontalArrangement", "start"),
                    node.float("spacing", 8f).dp
                )
            ) {
                renderChildren(node, context)
            }
        },

        "box" to { node, context, modifier ->
            Box(
                modifier = styledContainer(
                    node,
                    modifier,
                    node.color("backgroundColor", Color.Transparent)
                )
            ) {
                renderChildren(node, context)
            }
        },

        "card" to { node, context, modifier ->
            Card(
                modifier = modifier.then(
                    sizeModifier(
                        node.string("width", "match_parent"),
                        node.string("height")
                    )
                ),
                shape = node.shape("cornerRadius", 16f),
                colors = CardDefaults.cardColors(
                    containerColor = node.color("backgroundColor", AppColors.surface),
                    contentColor = node.color(
                        "contentColor",
                        MaterialTheme.colorScheme.onSurface
                    )
                ),
                border = if (node.float("borderWidth", 0f) > 0f) {
                    androidx.compose.foundation.BorderStroke(
                        node.float("borderWidth", 0f).dp,
                        node.color("borderColor", MaterialTheme.colorScheme.outline)
                    )
                } else {
                    null
                },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = node.float("elevation", 0f).dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(node.paddingValues("padding", 16)),
                    verticalArrangement = Arrangement.spacedBy(
                        node.float("spacing", 8f).dp
                    )
                ) {
                    renderChildren(node, context)
                }
            }
        },

        "lazy_column" to { node, context, modifier ->
            val source = node.string("source")
            val padding = node["padding"]?.jsonPrimitive?.floatOrNull ?: 0f
            val spacing = node["spacing"]?.jsonPrimitive?.floatOrNull ?: 0f
            val reverseLayout = node["reverseLayout"]?.jsonPrimitive?.booleanOrNull ?: false
            val userScrollEnabled = node["userScrollEnabled"]?.jsonPrimitive?.booleanOrNull ?: true

            val children = (node["children"] as? JsonArray)
                ?.mapNotNull { it as? JsonObject }
                .orEmpty()

            val displayChildren = if (reverseLayout) {
                children.reversed()
            } else {
                children
            }

            if (source.isNullOrBlank()) {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(padding.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.dp),
                    userScrollEnabled = userScrollEnabled
                ) {
                    items(displayChildren) { child ->
                        BduiRenderer(
                            node = child,
                            context = context
                        )
                    }
                }
            } else {
                val items = context.data[source] as? JsonArray
                    ?: JsonArray(emptyList())

                val indexedItems = items.mapIndexed { index, item ->
                    index to item
                }

                val displayItems = if (reverseLayout) {
                    indexedItems.reversed()
                } else {
                    indexedItems
                }

                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(padding.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.dp),
                    userScrollEnabled = userScrollEnabled
                ) {
                    items(displayItems) { (originalIndex, item) ->
                        val itemContext = context.withItem(item, originalIndex)

                        children.forEach { child ->
                            BduiRenderer(
                                node = child,
                                context = itemContext
                            )
                        }
                    }
                }
            }
        },

        "lazy_row" to { node, context, modifier ->
            val source = node.string("source")
            val padding = node["padding"]?.jsonPrimitive?.floatOrNull ?: 0f
            val spacing = node["spacing"]?.jsonPrimitive?.floatOrNull ?: 0f
            val reverseLayout = node["reverseLayout"]?.jsonPrimitive?.booleanOrNull ?: false
            val userScrollEnabled = node["userScrollEnabled"]?.jsonPrimitive?.booleanOrNull ?: true

            val children = (node["children"] as? JsonArray)
                ?.mapNotNull { it as? JsonObject }
                .orEmpty()

            val displayChildren = if (reverseLayout) {
                children.reversed()
            } else {
                children
            }

            if (source.isNullOrBlank()) {
                LazyRow(
                    modifier = modifier,
                    contentPadding = PaddingValues(padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                    userScrollEnabled = userScrollEnabled
                ) {
                    items(displayChildren) { child ->
                        BduiRenderer(
                            node = child,
                            context = context
                        )
                    }
                }
            } else {
                val items = context.data[source] as? JsonArray
                    ?: JsonArray(emptyList())

                val indexedItems = items.mapIndexed { index, item ->
                    index to item
                }

                val displayItems = if (reverseLayout) {
                    indexedItems.reversed()
                } else {
                    indexedItems
                }

                LazyRow(
                    modifier = modifier,
                    contentPadding = PaddingValues(padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                    userScrollEnabled = userScrollEnabled
                ) {
                    items(displayItems) { (originalIndex, item) ->
                        val itemContext = context.withItem(item, originalIndex)

                        children.forEach { child ->
                            BduiRenderer(
                                node = child,
                                context = itemContext
                            )
                        }
                    }
                }
            }
        },
    )
}

private fun verticalArrangement(value: String, spacing: androidx.compose.ui.unit.Dp): Arrangement.Vertical = when (value) {
    "center" -> Arrangement.Center
    "bottom" -> Arrangement.Bottom
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.spacedBy(spacing)
}

private fun horizontalArrangement(value: String, spacing: androidx.compose.ui.unit.Dp): Arrangement.Horizontal = when (value) {
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.spacedBy(spacing)
}

@Composable
private fun styledContainer(node: JsonObject, base: Modifier, background: Color): Modifier {
    var result = base.then(sizeModifier(node.string("width", "match_parent"), node.string("height")))
    val radius = node.float("cornerRadius", 0f)
    if (radius > 0f) result = result.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius.dp))
    if (background != Color.Transparent) result = result.background(background)
    result = result.padding(node.paddingValues())
    return result
}

@Composable
private fun renderChildren(node: JsonObject, context: BduiScreenContext) {
    node["children"]?.let { children ->
        (children as? JsonArray)?.forEach { child ->
            (child as? JsonObject)?.let { BduiRenderer(node = it, context = context) }
        }
    }
}
