package com.example.debt.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.debt.data.local.PreferenceCache
import com.example.debt.ui.utils.findActivity

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, COLOR
}

@SuppressLint("RememberReturnType")
@Composable
fun AppTheme(
    isSystemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val view = LocalView.current
    val window = view.context.findActivity().window
    val insetsController = WindowCompat.getInsetsController(window, view)

    val colorScheme = remember(PreferenceCache.themeChanged.value) {
        when (PreferenceCache.selectedTheme) {
            ThemeMode.COLOR -> if (isSystemDark) DarkRandomColorScheme else LightRandomColorScheme
            ThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
            ThemeMode.DARK -> DarkColorScheme
            ThemeMode.LIGHT -> LightColorScheme
        }
    }

    SideEffect {
        window.statusBarColor = colorScheme.background.toArgb()
        insetsController.isAppearanceLightStatusBars =
            colorScheme != DarkColorScheme && colorScheme != DarkRandomColorScheme
        window.navigationBarColor = colorScheme.background.toArgb()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

fun cardColorById(id: String): Color {
    val index = (id.hashCode() and Int.MAX_VALUE) % darkBGColors.size
    return if (PreferenceCache.isDarkTheme) {
        darkBGColors[index]
    } else {
        lightBGColors[index]
    }
}

fun interfaceColorById(id: String): Color {
    val index = (id.hashCode() and Int.MAX_VALUE) % darkBGColors.size
    return darkBGColors[index]
}

val lightBGColors = listOf(
    Color(0xFFF8BBD0), // light pink
    Color(0xFFD1C4E9), // light purple
    Color(0xFFBBDEFB), // light blue
    Color(0xFFB2EBF2), // light cyan
    Color(0xFFB2DFDB), // light teal
    Color(0xFFC8E6C9), // light green
    Color(0xFFF0F4C3), // light lime
    Color(0xFFFFF9C4), // light yellow
    Color(0xFFFFE0B2), // light orange
    Color(0xFFFFCCBC), // light deep orange
    Color(0xFFE1BEE7), // light light purple
    Color(0xFFCFD8DC)  // light blue grey
)

val darkBGColors = listOf(
    Color(0xFFD17F94), // dusty rose
    Color(0xFFA58CC9), // muted purple
    Color(0xFF7FA8D1), // denim blue
    Color(0xFF7DB8C0), // teal grey
    Color(0xFF7FAFA7), // slate teal
    Color(0xFF96BFA4), // sage green
    Color(0xFFC5C78D), // olive sand
    Color(0xFFD9C87B), // golden sand
    Color(0xFFD9B17D), // amber sand
    Color(0xFFD99E8A), // terracotta
    Color(0xFFB58CC2), // dusty lavender
    Color(0xFF9FAFBA)  // storm grey
)
