package com.local.hyperoswhitelistkeeper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.keeperDataStore by preferencesDataStore(name = "keeper_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun next(): ThemeMode = when (this) {
        SYSTEM -> LIGHT
        LIGHT -> DARK
        DARK -> SYSTEM
    }
}

enum class AppLanguage {
    VI,
    EN;

    fun next(): AppLanguage = if (this == VI) EN else VI
}

class PreferencesRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.applicationContext.keeperDataStore)

    val selectedPackages: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SELECTED_PACKAGES]?.toSet() ?: AppCatalog.defaultSelection
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        preferences[THEME_MODE]
            ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
            ?: ThemeMode.SYSTEM
    }

    val appLanguage: Flow<AppLanguage> = dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE]
            ?.let { stored -> AppLanguage.entries.firstOrNull { it.name == stored } }
            ?: AppLanguage.VI
    }

    suspend fun loadSelectedPackages(): Set<String> = selectedPackages.first()

    suspend fun saveSelectedPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[SELECTED_PACKAGES] = packages.toSet()
        }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    suspend fun saveAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language.name
        }
    }

    private companion object {
        val SELECTED_PACKAGES = stringSetPreferencesKey("selected_packages")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }
}
