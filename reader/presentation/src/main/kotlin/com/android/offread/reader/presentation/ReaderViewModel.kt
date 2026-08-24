package com.android.offread.reader.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.reader.domain.model.ReaderSettings
import com.android.offread.reader.domain.usecase.GetChapterContentUseCase
import com.android.offread.reader.domain.usecase.RetrySegmentUseCase
import com.android.offread.reader.domain.usecase.SaveReadingProgressUseCase
import com.android.offread.terms.domain.usecase.UpsertTermUseCase
import com.android.offread.translate.domain.TermCandidateExtractor
import com.android.offread.translate.domain.usecase.InvalidateChapterCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-015 웹소설 리더. [start] 로 아이템·시작 화를 받아 본문을 불러오고 이전/다음 화 이동·재시도·위치 저장을
 * 처리한다. 본문 롱프레스로 용어를 바로 고치는 흐름(F-017)도 여기서 다룬다.
 */
@HiltViewModel
class ReaderViewModel
    @Inject
    constructor(
        private val getItem: GetItemUseCase,
        private val getChapterContent: GetChapterContentUseCase,
        private val retrySegment: RetrySegmentUseCase,
        private val saveReadingProgress: SaveReadingProgressUseCase,
        private val upsertTerm: UpsertTermUseCase,
        private val invalidateChapterCache: InvalidateChapterCacheUseCase,
        private val termCandidateExtractor: TermCandidateExtractor,
    ) : MviViewModel<ReaderIntent, ReaderUiState, ReaderEvent, ReaderEffect>(ReaderUiState()) {
        private var itemId: String = ""
        private var collectionId: String = ""
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
                    collectionId = item.collectionId
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
                is ReaderIntent.LongPressSegment -> openWordPicker(intent.segmentId)
                is ReaderIntent.PickWord -> dispatch(ReaderEvent.QuickEditChanged(TermQuickEdit.Edit(intent.word)))
                is ReaderIntent.SubmitTerm -> submitTerm(intent)
                is ReaderIntent.ConfirmRetranslate -> confirmRetranslate(intent.retranslate)
                ReaderIntent.DismissQuickEdit -> dispatch(ReaderEvent.QuickEditChanged(null))
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

        /** F-017: 롱프레스한 세그먼트 원문에서 고를 만한 표기를 뽑는다. */
        private fun openWordPicker(segmentId: String) {
            val segment = currentState.content?.segments?.firstOrNull { it.id == segmentId } ?: return
            val words = termCandidateExtractor.wordsIn(segment.original)
            if (words.isEmpty()) {
                emitEffect(ReaderEffect.ShowError("고를 만한 표기를 찾지 못했어요."))
                return
            }
            dispatch(ReaderEvent.QuickEditChanged(TermQuickEdit.PickWord(words)))
        }

        private fun submitTerm(intent: ReaderIntent.SubmitTerm) {
            val source = (currentState.quickEdit as? TermQuickEdit.Edit)?.source ?: return
            viewModelScope.launch {
                upsertTerm(
                    UpsertTermUseCase.Input(
                        collectionId = collectionId,
                        source = source,
                        translation = intent.translation,
                        pinned = intent.pinned,
                    ),
                ).onSuccess { dispatch(ReaderEvent.QuickEditChanged(TermQuickEdit.ConfirmRetranslate)) }
                    .onFailure { emitEffect(ReaderEffect.ShowError(it.message ?: "저장에 실패했어요.")) }
            }
        }

        /** 새 용어를 반영하려면 이 챕터의 캐시를 버리고 다시 번역해야 한다(F-021). */
        private fun confirmRetranslate(retranslate: Boolean) {
            dispatch(ReaderEvent.QuickEditChanged(null))
            if (!retranslate) return
            val chapterIndex = currentState.chapterIndex
            viewModelScope.launch {
                invalidateChapterCache(itemId, chapterIndex)
                loadChapter(chapterIndex)
            }
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
                is ReaderEvent.QuickEditChanged -> state.copy(quickEdit = event.quickEdit)
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
