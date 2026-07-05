package com.android.offread.reader.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.reader.domain.model.ReaderSettings
import com.android.offread.reader.domain.usecase.GetChapterContentUseCase
import com.android.offread.reader.domain.usecase.RetrySegmentUseCase
import com.android.offread.reader.domain.usecase.SaveReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-015 웹소설 리더. [start] 로 아이템·시작 화를 받아 본문을 불러오고 이전/다음 화 이동·재시도·위치 저장을 처리한다.
 */
@HiltViewModel
class ReaderViewModel
    @Inject
    constructor(
        private val getItem: GetItemUseCase,
        private val getChapterContent: GetChapterContentUseCase,
        private val retrySegment: RetrySegmentUseCase,
        private val saveReadingProgress: SaveReadingProgressUseCase,
    ) : MviViewModel<ReaderIntent, ReaderUiState, ReaderEvent, ReaderEffect>(ReaderUiState()) {
        private var itemId: String = ""
        private var started = false

        fun start(
            itemId: String,
            chapterIndex: Int,
        ) {
            if (started) return
            started = true
            this.itemId = itemId
            viewModelScope.launch {
                getItem(itemId).first()?.let { item ->
                    dispatch(ReaderEvent.HeaderLoaded(item.title, item.totalChapters))
                }
                loadChapter(chapterIndex.coerceAtLeast(1))
            }
        }

        override fun onIntent(intent: ReaderIntent) {
            when (intent) {
                ReaderIntent.PreviousChapter -> if (currentState.hasPrevious) loadChapterAsync(currentState.chapterIndex - 1)
                ReaderIntent.NextChapter -> if (currentState.hasNext) loadChapterAsync(currentState.chapterIndex + 1)
                is ReaderIntent.RetrySegment -> retry(intent.segmentId)
                ReaderIntent.OpenSettings -> dispatch(ReaderEvent.SettingsVisible(true))
                ReaderIntent.CloseSettings -> dispatch(ReaderEvent.SettingsVisible(false))
                is ReaderIntent.ChangeFontScale ->
                    dispatch(
                        ReaderEvent.SettingsChanged(
                            currentState.settings.copy(
                                fontScale =
                                    intent.scale.coerceIn(
                                        ReaderSettings.MIN_FONT_SCALE,
                                        ReaderSettings.MAX_FONT_SCALE,
                                    ),
                            ),
                        ),
                    )
                is ReaderIntent.ChangeTheme -> dispatch(ReaderEvent.SettingsChanged(currentState.settings.copy(theme = intent.theme)))
            }
        }

        private fun loadChapterAsync(chapterIndex: Int) {
            viewModelScope.launch { loadChapter(chapterIndex) }
        }

        private suspend fun loadChapter(chapterIndex: Int) {
            dispatch(ReaderEvent.Loading(true))
            val content = getChapterContent(itemId, chapterIndex)
            dispatch(ReaderEvent.ContentLoaded(content))
            dispatch(ReaderEvent.Loading(false))
            // 읽던 위치 저장(F-015).
            saveReadingProgress(itemId, content.chapterIndex)
        }

        private fun retry(segmentId: String) {
            val chapter = currentState.chapterIndex
            viewModelScope.launch {
                runCatching { retrySegment(itemId, chapter, segmentId) }
                    .onSuccess { dispatch(ReaderEvent.SegmentRetried(segmentId, it)) }
                    .onFailure { emitEffect(ReaderEffect.ShowError(it.message ?: "재시도에 실패했어요.")) }
            }
        }

        override fun reduce(
            state: ReaderUiState,
            event: ReaderEvent,
        ): ReaderUiState =
            when (event) {
                is ReaderEvent.HeaderLoaded -> state.copy(itemTitle = event.title, totalChapters = event.totalChapters)
                is ReaderEvent.ContentLoaded -> state.copy(content = event.content)
                is ReaderEvent.Loading -> state.copy(loading = event.loading)
                is ReaderEvent.SegmentRetried -> state.copy(content = state.content?.withRetried(event.segmentId, event.translated))
                is ReaderEvent.SettingsVisible -> state.copy(settingsVisible = event.visible)
                is ReaderEvent.SettingsChanged -> state.copy(settings = event.settings)
            }
    }

private fun com.android.offread.reader.domain.model.ChapterContent.withRetried(
    segmentId: String,
    translated: String,
) = copy(
    segments = segments.map { if (it.id == segmentId) it.copy(translated = translated) else it },
)
