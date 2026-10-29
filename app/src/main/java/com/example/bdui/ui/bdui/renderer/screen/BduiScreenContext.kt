package com.example.bdui.ui.bdui.renderer.screen

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.navigation.NavController
import com.example.bdui.ui.bdui.actions.BduiActionHandler

class BduiScreenContext(
    val navController: NavController,

    // Необязательный заголовок текущего экрана
    val title: String? = null,

    // Общие значения полей формы.
    val values: SnapshotStateMap<String, String>,

    // Обработчик действий отделён от визуальных компонентов.
    val actionHandler: BduiActionHandler,

    // Аргументы именно текущего маршрута.
    val args: Map<String, String>
) {
    fun resolve(template: String): String {
        val pattern = Regex("""\{\{([^}]+)\}\}""")

        return pattern.replace(template) { match ->
            val key = match.groupValues[1].trim()

            if (key == "title") {
                title.orEmpty()
            } else {
                values[key] ?: args[key].orEmpty()
            }
        }
    }
}