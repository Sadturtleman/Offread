package com.android.offread.translate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.model.TranslationEngineKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 선택 엔진을 DataStore 에 영속한다. 기본값은 ML Kit — 모델 파일 없이 바로 번역되므로
 * 첫 실행에서 막히지 않는다.
 */
@Singleton
class DataStoreTranslationEnginePreference
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : TranslationEnginePreference {
        override val selected: Flow<TranslationEngineKind> =
            dataStore.data.map { prefs ->
                prefs[KEY_ENGINE]
                    ?.let { name -> runCatching { TranslationEngineKind.valueOf(name) }.getOrNull() }
                    ?: TranslationEngineKind.ML_KIT
            }

        override suspend fun select(kind: TranslationEngineKind) {
            dataStore.edit { prefs -> prefs[KEY_ENGINE] = kind.name }
        }

        private companion object {
            val KEY_ENGINE = stringPreferencesKey("translation_engine")
        }
    }
