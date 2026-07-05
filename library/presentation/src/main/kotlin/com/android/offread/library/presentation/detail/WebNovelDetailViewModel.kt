package com.android.offread.library.presentation.detail

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.usecase.GetChaptersUseCase
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.library.domain.usecase.PrepareOfflineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-008 웹소설 상세. [start] 로 아이템 id 를 받아 상세·챕터를 구독한다.
 */
@HiltViewModel
class WebNovelDetailViewModel
    @Inject
    constructor(
        private val getItem: GetItemUseCase,
        private val getChapters: GetChaptersUseCase,
        private val prepareOffline: PrepareOfflineUseCase,
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
        }

        override fun onIntent(intent: WebNovelDetailIntent) {
            when (intent) {
                WebNovelDetailIntent.PrepareOffline -> prepare()
                WebNovelDetailIntent.ContinueReading -> {
                    val item = currentState.item ?: return
                    emitEffect(WebNovelDetailEffect.OpenReader(item.id, item.lastReadChapter.coerceAtLeast(1)))
                }
                WebNovelDetailIntent.CheckForNewChapters ->
                    emitEffect(WebNovelDetailEffect.ShowMessage("새 화 확인은 곧 제공돼요."))
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
            }
    }
