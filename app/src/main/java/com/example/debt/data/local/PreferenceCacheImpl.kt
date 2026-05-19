package com.example.debt.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.debt.domain.local.PreferenceCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "THEME_PREFERENCES")

class PreferenceCacheImpl(
    private val context: Context
) : PreferenceCache {

    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore
    private val scope = CoroutineScope(Dispatchers.IO)

    private val SELECTED_THEME = stringPreferencesKey("selected_theme")
    private val USER_ID_KEY = stringPreferencesKey("user_id_key")
    private val TOKEN_KEY = stringPreferencesKey("token_key")

    override suspend fun saveAuth(userId: String, token: String) {
        dataStore.edit { prefs ->
            val encryptedUserId = if (userId.isNotEmpty()) {
                TokenCipher.encrypt(userId)
            } else {
                ""
            }
            val encryptedToken = if (token.isNotEmpty()) {
                TokenCipher.encrypt(token)
            } else {
                ""
            }

            if (encryptedUserId.isNotEmpty()) {
                prefs[USER_ID_KEY] = encryptedUserId
            } else {
                prefs.remove(USER_ID_KEY)
            }

            if (encryptedToken.isNotEmpty()) {
                prefs[TOKEN_KEY] = encryptedToken
            } else {
                prefs.remove(TOKEN_KEY)
            }
        }
    }

    override suspend fun setSelectedTheme(theme: Int) {
        dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = theme.toString()
        }
    }

    override suspend fun clearAuth() {
        dataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
            prefs.remove(TOKEN_KEY)
        }
    }

    override fun observeSelectedTheme(): Flow<Int> {
        return dataStore.data.map { prefs ->
            val themeName = prefs[SELECTED_THEME] ?: "0"
            themeName.toIntOrNull() ?: 0
        }
    }

    override fun observeUserId(): Flow<String> {
        return dataStore.data.map { prefs ->
            val encryptedId = prefs[USER_ID_KEY] ?: ""
            if (encryptedId.isNotEmpty()) {
                TokenCipher.decrypt(encryptedId) ?: ""
            } else {
                ""
            }
        }
    }

    override fun observeToken(): Flow<String> {
        return dataStore.data.map { prefs ->
            val encryptedToken = prefs[TOKEN_KEY] ?: ""
            if (encryptedToken.isNotEmpty()) {
                TokenCipher.decrypt(encryptedToken) ?: ""
            } else {
                ""
            }
        }
    }
}