package com.example.bdui.ui.bdui.renderer.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import kotlinx.serialization.json.JsonObject

typealias BduiComponentRenderer = @Composable (
    node: JsonObject,
    context: BduiScreenContext,
    modifier: Modifier
) -> Unit