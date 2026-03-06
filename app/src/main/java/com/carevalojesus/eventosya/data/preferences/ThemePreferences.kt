package com.carevalojesus.eventosya.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {
    private object Keys {
        val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
        val highContrastEnabled = booleanPreferencesKey("high_contrast_enabled")
    }

    val dynamicColorEnabled: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[Keys.dynamicColorEnabled] ?: false
    }

    val highContrastEnabled: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[Keys.highContrastEnabled] ?: false
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs: MutablePreferences ->
            prefs[Keys.dynamicColorEnabled] = enabled
        }
    }

    suspend fun setHighContrastEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs: MutablePreferences ->
            prefs[Keys.highContrastEnabled] = enabled
        }
    }
}
