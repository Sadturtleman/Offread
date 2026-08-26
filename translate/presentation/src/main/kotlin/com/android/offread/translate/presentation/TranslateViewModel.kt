package com.android.offread.translate.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.translate.domain.LlmModelStore
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.model.Segment
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.usecase.TranslatePageUseCase
import com.android.offread.translate.domain.usecase.TranslateSegmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 유일한 화면의 ViewModel. URL 을 받아 페이지를 번역하고, 엔진·모델 파일·캐시를 다룬다.
 *
 * MVP 는 일→한 고정이다. 다른 언어쌍은 웹페이지 언어 감지가 붙은 뒤에 연다.
 */
@HiltViewModel
class TranslateViewModel
    @Inject
    constructor(
        private val translatePage: TranslatePageUseCase,
        private val translateSegment: TranslateSegmentUseCase,
        private val enginePreference: TranslationEnginePreference,
        private val modelStore: LlmModelStore,
        private val cache: SegmentCache,
    ) : MviViewModel<TranslateIntent, TranslateUiState, TranslateEvent, TranslateEffect>(TranslateUiState()) {
        init {
            viewModelScope.launch {
                enginePreference.selected.collect { kind -> dispatch(TranslateEvent.EngineChanged(kind)) }
            }
            refreshModels()
            refreshCache()
        }

        override fun onIntent(intent: TranslateIntent) {
            when (intent) {
                is TranslateIntent.UrlChanged -> dispatch(TranslateEvent.UrlChanged(intent.url))
                TranslateIntent.Translate -> translate()
                is TranslateIntent.RetrySegment -> retry(intent.segmentId)
                TranslateIntent.OpenSettings -> dispatch(TranslateEvent.SettingsVisible(true))
                TranslateIntent.CloseSettings -> dispatch(TranslateEvent.SettingsVisible(false))
                is TranslateIntent.SelectEngine -> selectEngine(intent.kind)
                is TranslateIntent.ImportModel -> importModel(intent.uri)
                is TranslateIntent.DeleteModel -> deleteModel(intent.name)
                TranslateIntent.ClearCache -> clearCache()
            }
        }

        private fun translate() {
            val url = currentState.url.trim()
            if (url.isEmpty() || currentState.loading) return
            viewModelScope.launch {
                dispatch(TranslateEvent.Loading(true))
                runCatching { translatePage(url, PAIR) }
                    .onSuccess { page ->
                        dispatch(TranslateEvent.PageLoaded(page))
                        refreshCache()
                    }.onFailure {
                        emitEffect(TranslateEffect.ShowMessage(it.message ?: "번역하지 못했어요."))
                    }
                dispatch(TranslateEvent.Loading(false))
            }
        }

        private fun retry(segmentId: String) {
            val page = currentState.page ?: return
            if (currentState.retryingSegmentId != null) return
            val target = page.segments.firstOrNull { it.id == segmentId } ?: return
            viewModelScope.launch {
                dispatch(TranslateEvent.Retrying(segmentId))
                val result = translateSegment(Segment(target.id, target.original), page.languagePair)
                dispatch(TranslateEvent.SegmentRetried(segmentId, result.translated))
                if (result.translated == null) {
                    emitEffect(TranslateEffect.ShowMessage("이 문단을 번역하지 못했어요."))
                } else {
                    refreshCache()
                }
                dispatch(TranslateEvent.Retrying(null))
            }
        }

        private fun selectEngine(kind: TranslationEngineKind) {
            viewModelScope.launch {
                enginePreference.select(kind)
                emitEffect(TranslateEffect.ShowMessage("다음 번역부터 새 엔진을 써요."))
            }
        }

        private fun importModel(uri: String) {
            if (currentState.importing) return
            viewModelScope.launch {
                dispatch(TranslateEvent.Importing(true))
                runCatching { modelStore.import(uri) }
                    .onSuccess { file ->
                        refreshModels()
                        emitEffect(TranslateEffect.ShowMessage("${file.name} 을 가져왔어요."))
                    }.onFailure {
                        emitEffect(TranslateEffect.ShowMessage(it.message ?: "모델을 가져오지 못했어요."))
                    }
                dispatch(TranslateEvent.Importing(false))
            }
        }

        private fun deleteModel(name: String) {
            viewModelScope.launch {
                modelStore.delete(name)
                refreshModels()
                emitEffect(TranslateEffect.ShowMessage("모델 파일을 지웠어요."))
            }
        }

        private fun clearCache() {
            viewModelScope.launch {
                cache.clear()
                refreshCache()
                emitEffect(TranslateEffect.ShowMessage("번역 캐시를 비웠어요."))
            }
        }

        private fun refreshModels() {
            viewModelScope.launch { dispatch(TranslateEvent.ModelsChanged(modelStore.installed())) }
        }

        private fun refreshCache() {
            viewModelScope.launch { dispatch(TranslateEvent.CacheChanged(cache.stats())) }
        }

        override fun reduce(
            state: TranslateUiState,
            event: TranslateEvent,
        ): TranslateUiState =
            when (event) {
                is TranslateEvent.UrlChanged -> state.copy(url = event.url)
                is TranslateEvent.Loading -> state.copy(loading = event.loading)
                is TranslateEvent.PageLoaded -> state.copy(page = event.page)
                is TranslateEvent.SegmentRetried ->
                    state.copy(
                        page =
                            state.page?.let { page ->
                                page.copy(
                                    segments =
                                        page.segments.map { segment ->
                                            if (segment.id == event.segmentId) {
                                                segment.copy(translated = event.translated ?: segment.translated)
                                            } else {
                                                segment
                                            }
                                        },
                                )
                            },
                    )
                is TranslateEvent.Retrying -> state.copy(retryingSegmentId = event.segmentId)
                is TranslateEvent.SettingsVisible -> state.copy(settingsVisible = event.visible)
                is TranslateEvent.EngineChanged -> state.copy(engine = event.kind)
                is TranslateEvent.ModelsChanged -> state.copy(models = event.models)
                is TranslateEvent.Importing -> state.copy(importing = event.importing)
                is TranslateEvent.CacheChanged -> state.copy(cache = event.cache)
            }

        private companion object {
            /** MVP: 웹소설 일본어 → 한국어. */
            val PAIR = LanguagePair.JA_KO
        }
    }
