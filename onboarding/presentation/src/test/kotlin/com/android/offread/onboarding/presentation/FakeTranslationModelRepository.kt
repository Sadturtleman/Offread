package com.android.offread.onboarding.presentation

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.TranslationModelRepository
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 진행 상태를 테스트가 직접 주입하는 [TranslationModelRepository] 더블. */
class FakeTranslationModelRepository(
    installed: Set<LanguagePair> = emptySet(),
) : TranslationModelRepository {
    private val installedState = MutableStateFlow(installed)
    private val downloads = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())

    val enqueued = mutableListOf<TranslationModel>()
    val paused = mutableListOf<String>()
    val resumed = mutableListOf<String>()

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

    override val installedLanguagePairs: Flow<Set<LanguagePair>> = installedState.asStateFlow()

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

    fun emitDownloads(map: Map<String, ModelDownloadStatus>) {
        downloads.value = map
    }
}
