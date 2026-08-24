package com.android.offread.settings.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.translate.domain.TranslationModelRepository
import com.android.offread.translate.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 인메모리 [TranslationModelRepository] 더블(F-029 모델 관리 테스트용). */
class FakeTranslationModelRepository(
    installed: Set<LanguagePair> = emptySet(),
) : TranslationModelRepository {
    private val installedState = MutableStateFlow(installed)
    private val downloads = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())

    val enqueued = mutableListOf<TranslationModel>()
    val deleted = mutableListOf<String>()

    override fun catalogFor(pairs: Set<LanguagePair>): List<TranslationModel> =
        pairs.filter { it.isSelectable }.sortedBy { it.name }.map { pair -> model(pair) }

    override val installedLanguagePairs: Flow<Set<LanguagePair>> = installedState.asStateFlow()

    override fun observeDownloads(): Flow<Map<String, ModelDownloadStatus>> = downloads.asStateFlow()

    override suspend fun enqueue(models: List<TranslationModel>) {
        enqueued += models
        downloads.value = downloads.value + models.associate { it.id to ModelDownloadStatus.Queued }
    }

    override suspend fun pause(modelId: String) = Unit

    override suspend fun resume(modelId: String) = Unit

    override suspend fun delete(modelId: String) {
        deleted += modelId
        installedState.value = installedState.value.filterNot { model(it).id == modelId }.toSet()
        downloads.value = downloads.value - modelId
    }

    fun emitDownloads(map: Map<String, ModelDownloadStatus>) {
        downloads.value = map
    }

    companion object {
        fun model(pair: LanguagePair) =
            TranslationModel(
                id = "model-${pair.name.lowercase()}",
                languagePair = pair,
                displayName = "TranslateGemma 4B",
                sizeBytes = pair.modelSizeBytes,
                version = "1.0",
                sha256 = "test",
            )
    }
}
