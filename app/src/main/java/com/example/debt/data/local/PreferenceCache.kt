package com.example.debt.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import com.example.debt.App
import com.example.debt.ui.utils.LogUtils.errorLog
import com.example.debt.ui.theme.ThemeMode

object PreferenceCache {
    private val prefs: SharedPreferences = App.appInstance.getSharedPreferences(
        "THEME_PREFERENCES",
        Context.MODE_PRIVATE
    )

    private const val SELECTED_THEME: String = "selected_theme"
    private const val USER_ID_KEY: String = "user_id_key"
    private const val TOKEN_KEY: String = "token_key"

    @set:Synchronized
    var themeChanged = mutableStateOf(0)
        private set

    val isColoredTheme get() = selectedTheme == ThemeMode.COLOR
    val isDarkTheme get() = selectedTheme == ThemeMode.DARK

    @set:Synchronized
    var selectedTheme: ThemeMode
        get() = try {
            ThemeMode.valueOf(prefs.getString(SELECTED_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            errorLog("Failed to get theme\n$e")
            ThemeMode.SYSTEM
        }
        set(value) {
            prefs.edit().putString(SELECTED_THEME, value.name).apply()
            themeChanged.value++
        }

    @set:Synchronized
    var userId: String
        get() = prefs.getString(USER_ID_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(USER_ID_KEY, value).apply()
        }

    @set:Synchronized
    var token: String
        get() = prefs.getString(TOKEN_KEY, "") ?: ""
        set(value) {
            prefs.edit().putString(TOKEN_KEY, value).apply()
        }
}