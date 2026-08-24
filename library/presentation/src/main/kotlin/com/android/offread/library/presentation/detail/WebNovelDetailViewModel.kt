package com.android.offread.library.presentation.detail

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.usecase.GetChaptersUseCase
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.library.domain.usecase.MoveItemUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.PrepareOfflineUseCase
import com.android.offread.library.domain.usecase.RefreshChaptersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-008 웹소설 상세 + F-007 컬렉션 이동 + F-013 연재 업데이트 감지. [start] 로 아이템 id 를 받아 상세·챕터를 구독한다.
 */
@HiltViewModel
class WebNovelDetailViewModel
    @Inject
    constructor(
        private val getItem: GetItemUseCase,
        private val getChapters: GetChaptersUseCase,
        private val prepareOffline: PrepareOfflineUseCase,
        private val observeCollections: ObserveCollectionsUseCase,
        private val moveItem: MoveItemUseCase,
        private val refreshChapters: RefreshChaptersUseCase,
    ) : MviViewModel<WebNovelDetailIntent, WebNovelDetailUiState, WebNovelDetailEvent, WebNovelDetailEffect>(
            WebNovelDetailUiState(),
        ) {
        private var itemId: String = ""
        private var started = false

        fun start(itemId: String) {
            if (started) return
            started = true
            this.itemId = itemId
            viewModelScope.launch {
                getItem(itemId).filterNotNull().collect { item ->
                    dispatch(WebNovelDetailEvent.ItemLoaded(item, getChapters(item)))
                }
            }
            viewModelScope.launch {
                observeCollections(LibrarySort.NAME).collect { collections ->
                    dispatch(WebNovelDetailEvent.CollectionsChanged(collections))
                }
            }
        }

        override fun onIntent(intent: WebNovelDetailIntent) {
            when (intent) {
                WebNovelDetailIntent.PrepareOffline -> prepare()
                WebNovelDetailIntent.ContinueReading -> {
                    val item = currentState.item ?: return
                    emitEffect(WebNovelDetailEffect.OpenReader(item.id, item.lastReadChapter.coerceAtLeast(1)))
                }
                WebNovelDetailIntent.CheckForNewChapters -> checkForNewChapters()
                WebNovelDetailIntent.MoveClicked -> dispatch(WebNovelDetailEvent.MoveDialogChanged(true))
                WebNovelDetailIntent.DismissMoveDialog -> dispatch(WebNovelDetailEvent.MoveDialogChanged(false))
                is WebNovelDetailIntent.SubmitMove -> submitMove(intent)
            }
        }

        /** F-013: 원본 사이트에서 신규 화를 확인하고 목록을 갱신한다. */
        private fun checkForNewChapters() {
            if (currentState.refreshing) return
            viewModelScope.launch {
                dispatch(WebNovelDetailEvent.Refreshing(true))
                refreshChapters(itemId)
                    .onSuccess { added ->
                        val message =
                            if (added > 0) "새 ${added}화를 찾았어요." else "새로 올라온 화가 없어요."
                        emitEffect(WebNovelDetailEffect.ShowMessage(message))
                    }.onFailure {
                        emitEffect(WebNovelDetailEffect.ShowMessage(it.message ?: "새 화 확인에 실패했어요."))
                    }
                dispatch(WebNovelDetailEvent.Refreshing(false))
            }
        }

        private fun submitMove(intent: WebNovelDetailIntent.SubmitMove) {
            viewModelScope.launch {
                moveItem(itemId, intent.targetCollectionId, intent.strategy)
                    .onSuccess {
                        dispatch(WebNovelDetailEvent.MoveDialogChanged(false))
                        emitEffect(WebNovelDetailEffect.ShowMessage("컬렉션을 이동했어요."))
                    }.onFailure {
                        emitEffect(WebNovelDetailEffect.ShowMessage(it.message ?: "이동에 실패했어요."))
                    }
            }
        }

        private fun prepare() {
            if (currentState.preparing || itemId.isEmpty()) return
            viewModelScope.launch {
                dispatch(WebNovelDetailEvent.Preparing(true))
                prepareOffline(itemId)
                dispatch(WebNovelDetailEvent.Preparing(false))
                emitEffect(WebNovelDetailEffect.ShowMessage("다음 화를 오프라인용으로 준비했어요."))
            }
        }

        override fun reduce(
            state: WebNovelDetailUiState,
            event: WebNovelDetailEvent,
        ): WebNovelDetailUiState =
            when (event) {
                is WebNovelDetailEvent.ItemLoaded -> state.copy(item = event.item, chapters = event.chapters)
                is WebNovelDetailEvent.Preparing -> state.copy(preparing = event.preparing)
                is WebNovelDetailEvent.CollectionsChanged -> state.copy(collections = event.collections)
                is WebNovelDetailEvent.MoveDialogChanged -> state.copy(moveDialogVisible = event.visible)
                is WebNovelDetailEvent.Refreshing -> state.copy(refreshing = event.refreshing)
            }
    }
