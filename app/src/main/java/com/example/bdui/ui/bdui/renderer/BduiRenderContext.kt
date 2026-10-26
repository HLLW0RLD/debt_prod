package com.example.bdui.ui.bdui.renderer

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.navigation.NavController
import com.example.bdui.ui.bdui.actions.BduiActionHandler

class BduiRenderContext(
    val navController: NavController,

    // Общие значения полей формы.
    val values: SnapshotStateMap<String, String>,

    // Обработчик действий отделён от визуальных компонентов.
    val actionHandler: BduiActionHandler,

    // Аргументы именно текущего маршрута.
    val args: Map<String, String>
) {
    fun resolve(template: String): String {
        // {{name}} — подстановка из values, иначе из args маршрута
        val pattern = Regex("""\{\{([^}]+)\}\}""")

        return pattern.replace(template) { match ->
            val key = match.groupValues[1].trim()
            values[key] ?: args[key].orEmpty()
        }
    }
}