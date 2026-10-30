package com.example.bdui.ui.bdui.renderer.chrome

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import com.example.bdui.ui.items.CustomAppBar
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object BduiAppBarRegistry {

    @Composable
    fun render(
        node: JsonObject,
        context: BduiScreenContext
    ) {
        val type = node["type"]
            ?.let {
                if (it == JsonNull) {
                    "default"
                } else {
                    it.jsonPrimitive.content
                        .trim()
                        .ifEmpty { "default" }
                }
            }
            ?: "default"

        val title = context.resolve(
            node["title"]
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()
        )

        val showBackButton = node["showBackButton"]
            ?.jsonPrimitive
            ?.booleanOrNull
            ?: (type == "search")

        val height = node["height"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.toFloatOrNull()
            ?: 56f

        val textSize = node["textSize"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.toFloatOrNull()
            ?: 24f

        val showDivider = node["showDivider"]
            ?.jsonPrimitive
            ?.booleanOrNull
            ?: true

        val isCentered = node["isCentered"]
            ?.jsonPrimitive
            ?.booleanOrNull
            ?: true

        val optionsMode = node["optionsMode"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.ifEmpty { "menu" }
            ?: "menu"

        val showOptions = optionsMode == "button" || !node["options"]?.jsonArray.isNullOrEmpty()

        var menuExpanded by remember { mutableStateOf(false) }

        val key = node["valueKey"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.ifEmpty { "searchQuery" }
            ?: "searchQuery"

        val query = context.values[key].orEmpty()

        val placeholder = context.resolve(
            node["placeholder"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?: "Поиск"
        )

        val androidContext = LocalContext.current
        val backPainter = painterForName(androidContext, node["backIcon"]?.jsonPrimitive?.contentOrNull ?: "arrow_left", painterResource(R.drawable.arrow_left))
        val optionsPainter = painterForName(androidContext, node["optionsIcon"]?.jsonPrimitive?.contentOrNull ?: "switcher_options", painterResource(R.drawable.switcher_options))

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            CustomAppBar(
                type = type,
                title = title,
                showBack = showBackButton,
                showOptions = showOptions,
                isCentered = isCentered,
                showDivider = showDivider,
                height = height,
                textSize = textSize,
                query = query,
                placeholder = placeholder,
                backgroundColor = node["backgroundColor"]?.let { com.example.bdui.ui.bdui.renderer.components.parseBduiColor(it.jsonPrimitive.contentOrNull, AppColors.background) } ?: AppColors.background,
                titleColor = node["titleColor"]?.let { com.example.bdui.ui.bdui.renderer.components.parseBduiColor(it.jsonPrimitive.contentOrNull, AppColors.textPrimary) } ?: AppColors.textPrimary,
                backIconTint = node["backIconTint"]?.let { com.example.bdui.ui.bdui.renderer.components.parseBduiColor(it.jsonPrimitive.contentOrNull, AppColors.textPrimary) } ?: AppColors.textPrimary,
                optionsIconTint = node["optionsIconTint"]?.let { com.example.bdui.ui.bdui.renderer.components.parseBduiColor(it.jsonPrimitive.contentOrNull, AppColors.textPrimary) } ?: AppColors.textPrimary,
                backIcon = backPainter,
                optionsIcon = optionsPainter,
                onQueryChange = if (type == "search") {
                    { value -> context.values[key] = value }
                } else {
                    null
                },
                onBackClick = {
                    context.navController.popBackStack()
                },
                onOptionsClick = {
                    when (optionsMode) {
                        "menu" -> menuExpanded = true

                        "button" -> {
                            val action = node["action"] as? JsonObject
                            action?.let {
                                context.executeAction(it)
                            }
                        }
                    }
                }
            )

            if (showOptions && optionsMode == "menu") {
                Box {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        node["options"]
                            ?.jsonArray
                            ?.forEach { element ->
                                val option = element as? JsonObject
                                    ?: return@forEach

                                val label = option["label"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                    .orEmpty()

                                val action = option["action"] as? JsonObject

                                DropdownMenuItem(
                                    text = {
                                        Text(context.resolve(label))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        action?.let {
                                            context.executeAction(it)
                                        }
                                    }
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun painterForName(context: Context, name: String, fallback: Painter): Painter {
    val resourceName = name.substringAfterLast('/').substringBeforeLast('.').lowercase()
    val id = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    return if (id != 0) painterResource(id) else fallback
}
