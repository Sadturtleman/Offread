package com.android.offread.translate.presentation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.model.TranslatedPage
import com.android.offread.translate.domain.model.TranslationEngineKind

data class TranslateUiState(
    val url: String = "",
    val page: TranslatedPage? = null,
    val loading: Boolean = false,
    val settingsVisible: Boolean = false,
    val engine: TranslationEngineKind = TranslationEngineKind.ML_KIT,
    val models: List<LlmModelFile> = emptyList(),
    val importing: Boolean = false,
    val cache: CacheStats = CacheStats.EMPTY,
    /** 재번역 중인 문단 id. 버튼 중복 탭을 막는다. */
    val retryingSegmentId: String? = null,
) : UiState {
    val canTranslate: Boolean get() = url.isNotBlank() && !loading
}

sealed interface TranslateIntent : MviIntent {
    data class UrlChanged(
        val url: String,
    ) : TranslateIntent

    data object Translate : TranslateIntent

    data class RetrySegment(
        val segmentId: String,
    ) : TranslateIntent

    data object OpenSettings : TranslateIntent

    data object CloseSettings : TranslateIntent

    data class SelectEngine(
        val kind: TranslationEngineKind,
    ) : TranslateIntent

    data class ImportModel(
        val uri: String,
    ) : TranslateIntent

    data class DeleteModel(
        val name: String,
    ) : TranslateIntent

    data object ClearCache : TranslateIntent
}

sealed interface TranslateEvent : ReducerEvent {
    data class UrlChanged(
        val url: String,
    ) : TranslateEvent

    data class Loading(
        val loading: Boolean,
    ) : TranslateEvent

    data class PageLoaded(
        val page: TranslatedPage,
    ) : TranslateEvent

    data class SegmentRetried(
        val segmentId: String,
        val translated: String?,
    ) : TranslateEvent

    data class Retrying(
        val segmentId: String?,
    ) : TranslateEvent

    data class SettingsVisible(
        val visible: Boolean,
    ) : TranslateEvent

    data class EngineChanged(
        val kind: TranslationEngineKind,
    ) : TranslateEvent

    data class ModelsChanged(
        val models: List<LlmModelFile>,
    ) : TranslateEvent

    data class Importing(
        val importing: Boolean,
    ) : TranslateEvent

    data class CacheChanged(
        val cache: CacheStats,
    ) : TranslateEvent
}

sealed interface TranslateEffect : MviEffect {
    data class ShowMessage(
        val message: String,
    ) : TranslateEffect
}
