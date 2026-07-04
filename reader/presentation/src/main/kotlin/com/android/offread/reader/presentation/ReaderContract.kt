package com.android.offread.reader.presentation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSettings

data class ReaderUiState(
    val itemTitle: String = "",
    val totalChapters: Int = 0,
    val content: ChapterContent? = null,
    val loading: Boolean = true,
    val settings: ReaderSettings = ReaderSettings(),
    val settingsVisible: Boolean = false,
) : UiState {
    val chapterIndex: Int get() = content?.chapterIndex ?: 0
    val hasPrevious: Boolean get() = chapterIndex > 1
    val hasNext: Boolean get() = chapterIndex < totalChapters
}

sealed interface ReaderIntent : MviIntent {
    data object PreviousChapter : ReaderIntent

    data object NextChapter : ReaderIntent

    data class RetrySegment(
        val segmentId: String,
    ) : ReaderIntent

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
