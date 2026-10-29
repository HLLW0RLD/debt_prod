package com.example.bdui.ui.bdui.renderer.chrome

import androidx.compose.runtime.Composable
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import com.example.bdui.ui.items.CustomBottomBar
import com.example.bdui.ui.items.CustomBottomBarItem
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object BduiBottomBarRegistry {

    @Composable
    fun render(
        node: JsonObject,
        context: BduiScreenContext
    ) {
        val items = node["items"]?.jsonArray?.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null

            CustomBottomBarItem(
                id = item["id"]?.jsonPrimitive
                    ?.contentOrNull.orEmpty(),
                label = context.resolve(
                    item["label"]?.jsonPrimitive
                        ?.contentOrNull.orEmpty()
                ),
                icon = item["icon"]?.jsonPrimitive
                    ?.contentOrNull.orEmpty()
            )
        }.orEmpty()

        val selectedItem = node["selectedItem"]
            ?.jsonPrimitive
            ?.contentOrNull

        CustomBottomBar(
            items = items,
            selectedItem = selectedItem,
            onItemClick = { selected ->
                val item = node["items"]
                    ?.jsonArray
                    ?.firstOrNull { element ->
                        val jsonItem = element as? JsonObject
                        jsonItem?.get("id")
                            ?.jsonPrimitive
                            ?.contentOrNull == selected.id
                    } as? JsonObject

                val action = item?.get("action") as? JsonObject

                if (action != null) {
                    context.actionHandler.executeTabNavigationAction(action)
                }
            }
        )
    }
}