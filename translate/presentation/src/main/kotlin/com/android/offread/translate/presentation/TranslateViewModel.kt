package com.android.offread.translate.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.translate.domain.LlmModelDownloader
import com.android.offread.translate.domain.LlmModelStore
import com.android.offread.translate.domain.ModelDownloadState
import com.android.offread.translate.domain.NetworkStatus
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.model.VisibleText
import com.android.offread.translate.domain.usecase.TranslateTextsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 유일한 화면의 ViewModel. 원문 페이지를 웹뷰에 띄우고, 거기서 긁어 온 텍스트를 번역해
 * 제자리에 돌려보낸다. 엔진·모델 파일·캐시도 여기서 다룬다.
 *
 * MVP 는 일→한 고정이다. 다른 언어쌍은 웹페이지 언어 감지가 붙은 뒤에 연다.
 */
@HiltViewModel
class TranslateViewModel
    @Inject
    constructor(
        private val translateTexts: TranslateTextsUseCase,
        private val enginePreference: TranslationEnginePreference,
        private val modelStore: LlmModelStore,
        private val cache: SegmentCache,
        private val downloader: LlmModelDownloader,
        private val networkStatus: NetworkStatus,
    ) : MviViewModel<TranslateIntent, TranslateUiState, TranslateEvent, TranslateEffect>(
            TranslateUiState(modelSizeBytes = downloader.release.sizeBytes),
        ) {
        private var downloadJob: Job? = null
        private var translateJob: Job? = null

        /** 웹뷰가 마지막으로 넘겨 준 노드들. 실패한 것만 다시 돌릴 때 쓴다. */
        private var collected: List<VisibleText> = emptyList()
        private val failedIds = mutableSetOf<String>()

        init {
            viewModelScope.launch {
                enginePreference.selected.collect { kind -> dispatch(TranslateEvent.EngineChanged(kind)) }
            }
            viewModelScope.launch {
                dispatch(TranslateEvent.ModelsChanged(modelStore.installed()))
                // 모델이 없으면 실행하자마자 받아 둔다. 2GB 라 종량제 망에서는 사용자가 직접 누르게 한다.
                if (currentState.modelMissing && networkStatus.isUnmetered()) startDownload()
            }
            refreshCache()
        }

        override fun onIntent(intent: TranslateIntent) {
            when (intent) {
                is TranslateIntent.UrlChanged -> dispatch(TranslateEvent.UrlChanged(intent.url))
                TranslateIntent.Translate -> openPage()
                TranslateIntent.PageLoaded -> dispatch(TranslateEvent.PageLoaded)
                is TranslateIntent.TextsCollected -> translate(intent.texts)
                TranslateIntent.RetryFailed -> translate(collected.filter { it.id in failedIds })
                TranslateIntent.OpenSettings -> dispatch(TranslateEvent.SettingsVisible(true))
                TranslateIntent.CloseSettings -> dispatch(TranslateEvent.SettingsVisible(false))
                is TranslateIntent.SelectEngine -> selectEngine(intent.kind)
                is TranslateIntent.ImportModel -> importModel(intent.uri)
                is TranslateIntent.DeleteModel -> deleteModel(intent.name)
                TranslateIntent.ClearCache -> clearCache()
                TranslateIntent.DownloadModel -> startDownload()
                TranslateIntent.CancelDownload -> cancelDownload()
            }
        }

        /** 주소를 웹뷰에 넘긴다. 실제 수집은 페이지가 다 뜬 뒤 웹뷰가 시작한다. */
        private fun openPage() {
            val url = currentState.url.trim()
            if (url.isEmpty() || currentState.loading) return
            translateJob?.cancel()
            collected = emptyList()
            failedIds.clear()
            dispatch(TranslateEvent.PageRequested(url.withScheme()))
        }

        /**
         * 노드를 하나씩 번역해 그때그때 페이지에 돌려보낸다. 다 끝나기를 기다리지 않으므로
         * 긴 글도 위에서부터 한국어로 바뀐다.
         */
        private fun translate(texts: List<VisibleText>) {
            if (texts.isEmpty()) {
                dispatch(TranslateEvent.Collected(0))
                return
            }
            if (collected.isEmpty()) collected = texts
            failedIds -= texts.map { it.id }.toSet()
            translateJob?.cancel()
            dispatch(TranslateEvent.Collected(texts.size))
            translateJob =
                viewModelScope.launch {
                    translateTexts(texts, PAIR)
                        .catch { error ->
                            emitEffect(TranslateEffect.ShowMessage(error.message ?: "번역하지 못했어요."))
                        }.collect { result ->
                            val text = result.translated
                            if (text == null) {
                                failedIds += result.id
                            } else {
                                emitEffect(TranslateEffect.ApplyTranslation(result.id, text))
                            }
                            dispatch(TranslateEvent.TextTranslated(success = text != null))
                        }
                    refreshCache()
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

        /**
         * 모델을 내려받는다. 이어받기는 어댑터가 하므로 여기서는 다시 부르기만 하면 된다.
         * 화면을 떠나면 viewModelScope 와 함께 멈추고, 다음 실행에서 받던 자리부터 잇는다.
         */
        private fun startDownload() {
            if (downloadJob?.isActive == true) return
            downloadJob =
                viewModelScope.launch {
                    downloader
                        .download()
                        .catch { error ->
                            dispatch(TranslateEvent.DownloadChanged(null))
                            emitEffect(TranslateEffect.ShowMessage(error.message ?: "모델을 받지 못했어요."))
                        }.collect { state ->
                            when (state) {
                                is ModelDownloadState.Running -> dispatch(TranslateEvent.DownloadChanged(state))
                                is ModelDownloadState.Completed -> {
                                    dispatch(TranslateEvent.DownloadChanged(null))
                                    refreshModels()
                                    emitEffect(TranslateEffect.ShowMessage("번역 모델을 받았어요."))
                                }
                            }
                        }
                }
        }

        private fun cancelDownload() {
            downloadJob?.cancel()
            downloadJob = null
            dispatch(TranslateEvent.DownloadChanged(null))
            emitEffect(TranslateEffect.ShowMessage("받던 만큼은 남겨 뒀어요. 다시 누르면 이어서 받아요."))
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
                is TranslateEvent.PageRequested ->
                    state.copy(loadedUrl = event.url, loading = true, total = 0, translated = 0, failed = 0)
                is TranslateEvent.PageLoaded -> state.copy(loading = false)
                is TranslateEvent.Collected -> state.copy(total = event.total, translated = 0, failed = 0)
                is TranslateEvent.TextTranslated ->
                    if (event.success) state.copy(translated = state.translated + 1) else state.copy(failed = state.failed + 1)
                is TranslateEvent.SettingsVisible -> state.copy(settingsVisible = event.visible)
                is TranslateEvent.EngineChanged -> state.copy(engine = event.kind)
                is TranslateEvent.ModelsChanged -> state.copy(models = event.models)
                is TranslateEvent.Importing -> state.copy(importing = event.importing)
                is TranslateEvent.CacheChanged -> state.copy(cache = event.cache)
                is TranslateEvent.DownloadChanged -> state.copy(download = event.download)
            }

        /** 주소만 적어도 열리게 한다. 웹뷰는 스킴 없는 문자열을 검색어로 보지 않는다. */
        private fun String.withScheme(): String = if (startsWith("http://") || startsWith("https://")) this else "https://$this"

        private companion object {
            /** MVP: 웹소설 일본어 → 한국어. */
            val PAIR = LanguagePair.JA_KO
        }
    }
