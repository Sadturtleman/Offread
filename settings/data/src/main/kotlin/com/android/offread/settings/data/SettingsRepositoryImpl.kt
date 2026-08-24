package com.android.offread.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.offread.settings.domain.SettingsRepository
import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.model.DisplayTheme
import com.android.offread.settings.domain.model.TranslationDisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** [SettingsRepository] 의 DataStore 어댑터. enum 은 name 으로 저장하며 알 수 없는 값은 기본값으로 대체한다. */
class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        override val displaySettings: Flow<DisplaySettings> =
            dataStore.data.map { prefs ->
                DisplaySettings(
                    fontScale = prefs[KEY_FONT_SCALE] ?: 1.0f,
                    theme = prefs[KEY_THEME].toThemeOrDefault(),
                    translationDisplay = prefs[KEY_TRANSLATION_DISPLAY].toModeOrDefault(),
                )
            }

        override suspend fun updateDisplaySettings(settings: DisplaySettings) {
            dataStore.edit { prefs ->
                prefs[KEY_FONT_SCALE] = settings.fontScale
                prefs[KEY_THEME] = settings.theme.name
                prefs[KEY_TRANSLATION_DISPLAY] = settings.translationDisplay.name
            }
        }

        private fun String?.toThemeOrDefault(): DisplayTheme =
            this?.let { runCatching { DisplayTheme.valueOf(it) }.getOrNull() } ?: DisplayTheme.LIGHT

        private fun String?.toModeOrDefault(): TranslationDisplayMode =
            this?.let { runCatching { TranslationDisplayMode.valueOf(it) }.getOrNull() }
                ?: TranslationDisplayMode.TRANSLATED_ONLY

        private companion object {
            val KEY_FONT_SCALE = floatPreferencesKey("display_font_scale")
            val KEY_THEME = stringPreferencesKey("display_theme")
            val KEY_TRANSLATION_DISPLAY = stringPreferencesKey("display_translation_mode")
        }
    }
