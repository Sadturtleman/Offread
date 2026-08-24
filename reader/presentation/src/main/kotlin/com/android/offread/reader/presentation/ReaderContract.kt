package com.android.offread.reader.presentation

import com.android.offread.core.entity.TranslationStatus
import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSettings

/**
 * F-017 용어 빠른편집 흐름. 롱프레스 → 단어 고르기 → 편집 → 현재 챕터 재번역 확인.
 */
sealed interface TermQuickEdit {
    /** 롱프레스한 세그먼트에서 고를 수 있는 표기들. */
    data class PickWord(
        val words: List<String>,
    ) : TermQuickEdit

    /** 고른 원어로 용어를 편집한다(T-02). */
    data class Edit(
        val source: String,
    ) : TermQuickEdit

    /** 저장 후 현재 챕터를 다시 번역할지 확인한다(캐시 무효화 동반). */
    data object ConfirmRetranslate : TermQuickEdit
}

data class ReaderUiState(
    val itemTitle: String = "",
    val totalChapters: Int = 0,
    val content: ChapterContent? = null,
    val loading: Boolean = true,
    val settings: ReaderSettings = ReaderSettings(),
    val settingsVisible: Boolean = false,
    val quickEdit: TermQuickEdit? = null,
) : UiState {
    val chapterIndex: Int get() = content?.chapterIndex ?: 0
    val hasPrevious: Boolean get() = chapterIndex > 1
    val hasNext: Boolean get() = chapterIndex < totalChapters
    val chapterStatus: TranslationStatus get() = content?.translationStatus ?: TranslationStatus.UNTRANSLATED
}

sealed interface ReaderIntent : MviIntent {
    data object PreviousChapter : ReaderIntent

    data object NextChapter : ReaderIntent

    data class RetrySegment(
        val segmentId: String,
    ) : ReaderIntent

    /** F-017: 본문 롱프레스. */
    data class LongPressSegment(
        val segmentId: String,
    ) : ReaderIntent

    data class PickWord(
        val word: String,
    ) : ReaderIntent

    data class SubmitTerm(
        val translation: String,
        val pinned: Boolean,
    ) : ReaderIntent

    data class ConfirmRetranslate(
        val retranslate: Boolean,
    ) : ReaderIntent

    data object DismissQuickEdit : ReaderIntent

    data object OpenSettings : ReaderIntent

    data object CloseSettings : ReaderIntent

    data class ChangeFontScale(
        val scale: Float,
    ) : ReaderIntent

    data class ChangeTheme(
        val theme: com.android.offread.reader.domain.model.ReaderTheme,
    ) : ReaderIntent
}

sealed interface ReaderEvent : ReducerEvent {
    data class HeaderLoaded(
        val title: String,
        val totalChapters: Int,
    ) : ReaderEvent

    data class ContentLoaded(
        val content: ChapterContent,
    ) : ReaderEvent

    data class Loading(
        val loading: Boolean,
    ) : ReaderEvent

    data class SegmentRetried(
        val segmentId: String,
        val translated: String,
    ) : ReaderEvent

    data class QuickEditChanged(
        val quickEdit: TermQuickEdit?,
    ) : ReaderEvent

    data class SettingsVisible(
        val visible: Boolean,
    ) : ReaderEvent

    data class SettingsChanged(
        val settings: ReaderSettings,
    ) : ReaderEvent
}

sealed interface ReaderEffect : MviEffect {
    data class ShowError(
        val message: String,
    ) : ReaderEffect
}
