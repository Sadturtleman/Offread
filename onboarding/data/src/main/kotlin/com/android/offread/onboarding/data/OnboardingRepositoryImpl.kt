package com.android.offread.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [OnboardingRepository] 의 DataStore 어댑터. 언어쌍은 enum name 집합으로 저장한다.
 * 알 수 없는 name(스키마 변경/다운그레이드 잔재)은 조용히 무시해 복원 안정성을 지킨다.
 */
class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : OnboardingRepository {
        override val isOnboardingComplete: Flow<Boolean> =
            dataStore.data.map { prefs -> prefs[KEY_COMPLETE] ?: false }

        override val selectedLanguagePairs: Flow<Set<LanguagePair>> =
            dataStore.data.map { prefs ->
                prefs[KEY_PAIRS]
                    .orEmpty()
                    .mapNotNull { name -> runCatching { LanguagePair.valueOf(name) }.getOrNull() }
                    .toSet()
            }

        override suspend fun setSelectedLanguagePairs(pairs: Set<LanguagePair>) {
            dataStore.edit { prefs -> prefs[KEY_PAIRS] = pairs.map(LanguagePair::name).toSet() }
        }

        override suspend fun setOnboardingComplete(complete: Boolean) {
            dataStore.edit { prefs -> prefs[KEY_COMPLETE] = complete }
        }

        private companion object {
            val KEY_COMPLETE = booleanPreferencesKey("onboarding_complete")
            val KEY_PAIRS = stringSetPreferencesKey("selected_language_pairs")
        }
    }
