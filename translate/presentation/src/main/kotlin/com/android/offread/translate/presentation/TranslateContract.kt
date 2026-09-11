package com.android.offread.translate.presentation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.ModelDownloadState
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.model.VisibleText

data class TranslateUiState(
    val url: String = "",
    /** 웹뷰에 띄운 주소. 비어 있으면 아직 아무것도 안 띄웠다. */
    val loadedUrl: String? = null,
    /** 페이지를 불러오는 중. */
    val loading: Boolean = false,
    /** 모아 온 텍스트 노드 수. 0 이면 번역할 것을 아직 못 찾았다. */
    val total: Int = 0,
    val translated: Int = 0,
    val failed: Int = 0,
    val settingsVisible: Boolean = false,
    val engine: TranslationEngineKind = TranslationEngineKind.TRANSLATE_GEMMA,
    val models: List<LlmModelFile> = emptyList(),
    val importing: Boolean = false,
    val cache: CacheStats = CacheStats.EMPTY,
    /** 진행 중인 모델 다운로드. 없으면 null. */
    val download: ModelDownloadState.Running? = null,
    /** 내려받을 모델 크기. 시작 전에 얼마나 큰지 보여 준다. */
    val modelSizeBytes: Long = 0L,
) : UiState {
    val canTranslate: Boolean get() = url.isNotBlank() && !loading

    /** 고른 엔진이 모델 파일을 요구하는데 아직 없다. 번역을 눌러도 실패한다. */
    val modelMissing: Boolean get() = engine.requiresModelFile && models.isEmpty()

    /** 아직 번역 중인 노드가 남아 있다. */
    val translating: Boolean get() = total > 0 && translated + failed < total

    val progress: Float get() = if (total <= 0) 0f else ((translated + failed).toFloat() / total).coerceIn(0f, 1f)
}

sealed interface TranslateIntent : MviIntent {
    data class UrlChanged(
        val url: String,
    ) : TranslateIntent

    data object Translate : TranslateIntent

    /** 웹뷰가 페이지를 다 불러왔다. */
    data object PageLoaded : TranslateIntent

    /** 웹뷰가 번역할 텍스트 노드를 모아 왔다. */
    data class TextsCollected(
        val texts: List<VisibleText>,
    ) : TranslateIntent

    /** 실패한 노드만 다시 번역한다. */
    data object RetryFailed : TranslateIntent

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

    data object DownloadModel : TranslateIntent

    data object CancelDownload : TranslateIntent
}

sealed interface TranslateEvent : ReducerEvent {
    data class UrlChanged(
        val url: String,
    ) : TranslateEvent

    data class PageRequested(
        val url: String,
    ) : TranslateEvent

    data object PageLoaded : TranslateEvent

    data class Collected(
        val total: Int,
    ) : TranslateEvent

    data class TextTranslated(
        val success: Boolean,
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

    data class DownloadChanged(
        val download: ModelDownloadState.Running?,
    ) : TranslateEvent
}

sealed interface TranslateEffect : MviEffect {
    data class ShowMessage(
        val message: String,
    ) : TranslateEffect

    /** 번역문을 페이지의 제자리에 채운다. 웹뷰가 자바스크립트로 적용한다. */
    data class ApplyTranslation(
        val id: String,
        val text: String,
    ) : TranslateEffect
}
