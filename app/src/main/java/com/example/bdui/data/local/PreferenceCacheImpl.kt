package com.example.bdui.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bdui.domain.local.PreferenceCache
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

    override suspend fun setSelectedTheme(theme: Int) {
        dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = theme.toString()
        }
    }

    override fun observeSelectedTheme(): Flow<Int> {
        return dataStore.data.map { prefs ->
            val themeName = prefs[SELECTED_THEME] ?: "0"
            themeName.toIntOrNull() ?: 0
        }
    }
}