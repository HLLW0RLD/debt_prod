package com.example.debt.data.local

import androidx.compose.runtime.mutableStateOf
import com.example.debt.domain.utils.LogUtils.errorLog
import com.example.debt.ui.theme.ThemeMode
import com.russhwolf.settings.Settings

object PreferenceCache {
    private val settings = Settings()

    private const val SELECTED_THEME = "selected_theme"
    private const val USER_ID_KEY = "user_id_key"
    private const val TOKEN_KEY = "token_key"

    @set:Synchronized
    var themeChanged = mutableStateOf(0)
        private set

    var selectedTheme: ThemeMode
        get() = try {
            ThemeMode.valueOf(
                settings.getString(
                    SELECTED_THEME,
                    ThemeMode.SYSTEM.name
                )
            )
        } catch (e: Exception) {
            errorLog("Failed to get theme\n$e")
            ThemeMode.SYSTEM
        }
        set(value) {
            settings.putString(
                SELECTED_THEME,
                value.name
            )

            themeChanged.value++
        }

    val isColoredTheme get() = selectedTheme == ThemeMode.COLOR
    val isDarkTheme get() = selectedTheme == ThemeMode.DARK

    var userId: String
        get() = try {
            settings.getStringOrNull(USER_ID_KEY)
                ?.let(TokenCipher::decrypt)
                ?: ""
        } catch (e: Exception) {
            errorLog("Failed to decrypt userId\n$e")
            ""
        }
        private set(value) {
            settings.putString(
                USER_ID_KEY,
                TokenCipher.encrypt(value)
            )
        }

    var token: String
        get() = try {
            settings.getStringOrNull(TOKEN_KEY)
                ?.let(TokenCipher::decrypt)
                ?: ""
        } catch (e: Exception) {
            errorLog("Failed to decrypt token\n$e")
            ""
        }
        private set(value) {
            settings.putString(
                TOKEN_KEY,
                TokenCipher.encrypt(value)
            )
        }

    fun saveAuth(
        userId: String,
        token: String
    ) {
        this.userId = userId
        this.token = token
    }

    fun clearAuth() {
        userId = ""
        token = ""
    }
}