package com.android.offread.importer.presentation.webnovel

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.importer.domain.model.WebNovelMetadata
import com.android.offread.importer.domain.usecase.RecognizeWebNovelUseCase
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.usecase.AddWebNovelUseCase
import com.android.offread.library.domain.usecase.CreateCollectionUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-012 웹소설 URL 가져오기. URL 인식 → 컬렉션 선택 → 아이템 저장.
 */
@HiltViewModel
class WebNovelImportViewModel
    @Inject
    constructor(
        private val recognizeWebNovel: RecognizeWebNovelUseCase,
        private val observeCollections: ObserveCollectionsUseCase,
        private val createCollection: CreateCollectionUseCase,
        private val addWebNovel: AddWebNovelUseCase,
    ) : MviViewModel<WebNovelImportIntent, WebNovelImportUiState, WebNovelImportEvent, WebNovelImportEffect>(
            WebNovelImportUiState(),
        ) {
        init {
            viewModelScope.launch {
                observeCollections(LibrarySort.RECENT).collect { collections ->
                    dispatch(WebNovelImportEvent.CollectionsChanged(collections))
                    // 선택된 컬렉션이 없으면 첫 컬렉션을 기본 선택한다.
                    if (currentState.selectedCollectionId == null) {
                        collections.firstOrNull()?.let { dispatch(WebNovelImportEvent.CollectionSelected(it.id)) }
                    }
                }
            }
        }

        override fun onIntent(intent: WebNovelImportIntent) {
            when (intent) {
                is WebNovelImportIntent.UrlChanged -> dispatch(WebNovelImportEvent.UrlChanged(intent.url))
                WebNovelImportIntent.Recognize -> recognize()
                is WebNovelImportIntent.SelectCollection -> dispatch(WebNovelImportEvent.CollectionSelected(intent.id))
                is WebNovelImportIntent.CreateCollection -> create(intent.name)
                WebNovelImportIntent.Import -> import()
            }
        }

        private fun recognize() {
            if (!currentState.canRecognize) return
            viewModelScope.launch {
                dispatch(WebNovelImportEvent.Recognizing(true))
                recognizeWebNovel(currentState.url)
                    .onSuccess { dispatch(WebNovelImportEvent.Recognized(it)) }
                    .onFailure {
                        dispatch(WebNovelImportEvent.Recognizing(false))
                        emitEffect(WebNovelImportEffect.ShowError(it.message ?: "인식에 실패했어요."))
                    }
            }
        }

        private fun create(name: String) {
            viewModelScope.launch {
                createCollection(name)
                    .onSuccess { id -> dispatch(WebNovelImportEvent.CollectionSelected(id)) }
                    .onFailure { emitEffect(WebNovelImportEffect.ShowError(it.message ?: "컬렉션 생성 실패")) }
            }
        }

        private fun import() {
            val metadata = currentState.metadata ?: return
            val collectionId = currentState.selectedCollectionId ?: return
            if (!currentState.canImport) return
            viewModelScope.launch {
                dispatch(WebNovelImportEvent.Importing(true))
                addWebNovel(metadata.toRequest(collectionId))
                    .onSuccess { emitEffect(WebNovelImportEffect.Done) }
                    .onFailure {
                        dispatch(WebNovelImportEvent.Importing(false))
                        emitEffect(WebNovelImportEffect.ShowError(it.message ?: "가져오기에 실패했어요."))
                    }
            }
        }

        override fun reduce(
            state: WebNovelImportUiState,
            event: WebNovelImportEvent,
        ): WebNovelImportUiState =
            when (event) {
                is WebNovelImportEvent.UrlChanged -> state.copy(url = event.url)
                is WebNovelImportEvent.Recognizing -> state.copy(recognizing = event.recognizing)
                is WebNovelImportEvent.Recognized -> state.copy(recognizing = false, metadata = event.metadata)
                is WebNovelImportEvent.CollectionsChanged -> state.copy(collections = event.collections)
                is WebNovelImportEvent.CollectionSelected -> state.copy(selectedCollectionId = event.id)
                is WebNovelImportEvent.Importing -> state.copy(importing = event.importing)
            }
    }

private fun WebNovelMetadata.toRequest(collectionId: String) =
    AddWebNovelUseCase.Request(
        collectionId = collectionId,
        title = title,
        author = author,
        sourceUrl = sourceUrl,
        siteName = siteName,
        totalChapters = totalChapters,
        serialStatus = serialStatus,
    )
