package com.example.bdui.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Общее состояние нижней панели BDUI.
 *
 * enabled - разрешена ли панель в целом
 * visible - должна ли панель отображаться на текущем экране
 */
class BottomBarState {
    var enabled by mutableStateOf(true)
    var visible by mutableStateOf(false)
}

val LocalBottomBarState =
    staticCompositionLocalOf<BottomBarState> {
        error("BottomBarState not provided")
    }