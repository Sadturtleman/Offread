package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 인메모리 [TranslationModelRepository] 테스트 더블. 카탈로그와 상태를 직접 제어한다. */
class FakeTranslationModelRepository(
    installed: Set<LanguagePair> = emptySet(),
) : TranslationModelRepository {
    private val installedState = MutableStateFlow(installed)
    private val downloads = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())

    val enqueued = mutableListOf<TranslationModel>()
    val paused = mutableListOf<String>()
    val resumed = mutableListOf<String>()

    override val installedLanguagePairs: Flow<Set<LanguagePair>> = installedState.asStateFlow()

    override fun catalogFor(pairs: Set<LanguagePair>): List<TranslationModel> =
        pairs.filter { it.isSelectable }.map { pair ->
            TranslationModel(
                id = "model-${pair.name.lowercase()}",
                languagePair = pair,
                displayName = "TranslateGemma 4B",
                sizeBytes = pair.modelSizeBytes,
                version = "1.0",
                sha256 = "test",
            )
        }

    override fun observeDownloads(): Flow<Map<String, ModelDownloadStatus>> = downloads.asStateFlow()

    override suspend fun enqueue(models: List<TranslationModel>) {
        enqueued += models
    }

    override suspend fun pause(modelId: String) {
        paused += modelId
    }

    override suspend fun resume(modelId: String) {
        resumed += modelId
    }

    /** 테스트에서 다운로드 상태를 임의로 갱신한다. */
    fun emitDownloads(map: Map<String, ModelDownloadStatus>) {
        downloads.value = map
    }
}
