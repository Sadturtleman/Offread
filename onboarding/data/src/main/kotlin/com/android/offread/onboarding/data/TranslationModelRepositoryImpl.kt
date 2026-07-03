package com.android.offread.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.TranslationModelRepository
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TranslationModelRepository] 어댑터.
 *
 * - 설치 완료 언어쌍은 DataStore 에 영속(F-001 분기·F-003 재설치 판별의 기준).
 * - 다운로드 진행은 현재 **시뮬레이터**로 구현한다(진행률·일시정지/재개·완료). 실제 WorkManager 백그라운드
 *   다운로드·range 이어받기·체크섬 검증·재시도 3회(F-003)는 후속 인프라 태스크에서 이 클래스를 교체한다.
 */
@Singleton
class TranslationModelRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : TranslationModelRepository {
        // 시뮬레이터 전용 백그라운드 스코프. 실제 어댑터에서는 WorkManager 로 대체된다.
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private val downloads = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())
        private val pausedIds = MutableStateFlow<Set<String>>(emptySet())
        private val jobs = mutableMapOf<String, Job>()

        override fun catalogFor(pairs: Set<LanguagePair>): List<TranslationModel> = ModelCatalog.forPairs(pairs)

        override val installedLanguagePairs: Flow<Set<LanguagePair>> =
            dataStore.data.map { prefs ->
                prefs[KEY_INSTALLED]
                    .orEmpty()
                    .mapNotNull { name -> runCatching { LanguagePair.valueOf(name) }.getOrNull() }
                    .toSet()
            }

        override fun observeDownloads(): Flow<Map<String, ModelDownloadStatus>> = downloads.asStateFlow()

        override suspend fun enqueue(models: List<TranslationModel>) {
            models.forEach { model ->
                if (jobs[model.id]?.isActive == true) return@forEach
                downloads.update { it + (model.id to ModelDownloadStatus.Queued) }
                jobs[model.id] = scope.launch { simulateDownload(model) }
            }
        }

        override suspend fun pause(modelId: String) {
            pausedIds.update { it + modelId }
        }

        override suspend fun resume(modelId: String) {
            pausedIds.update { it - modelId }
        }

        private suspend fun simulateDownload(model: TranslationModel) {
            val total = model.sizeBytes.coerceAtLeast(1)
            // 데모용: 실제 크기와 무관하게 ~5초에 완료. 표시 속도는 tick 당 증가분에서 계산.
            val bytesPerSecond = total / DEMO_DURATION_SECONDS
            var downloaded = 0L
            while (downloaded < total) {
                if (model.id in pausedIds.value) {
                    downloads.update { it + (model.id to ModelDownloadStatus.Paused) }
                    delay(TICK_MILLIS)
                    continue
                }
                downloaded = (downloaded + bytesPerSecond * TICK_MILLIS / 1000).coerceAtMost(total)
                downloads.update {
                    it + (model.id to ModelDownloadStatus.Downloading(downloaded, total, bytesPerSecond))
                }
                delay(TICK_MILLIS)
            }
            downloads.update { it + (model.id to ModelDownloadStatus.Completed) }
            markInstalled(model.languagePair)
        }

        private suspend fun markInstalled(pair: LanguagePair) {
            dataStore.edit { prefs ->
                prefs[KEY_INSTALLED] = prefs[KEY_INSTALLED].orEmpty() + pair.name
            }
        }

        private companion object {
            val KEY_INSTALLED = stringSetPreferencesKey("installed_language_pairs")
            const val DEMO_DURATION_SECONDS = 5L
            const val TICK_MILLIS = 200L
        }
    }
