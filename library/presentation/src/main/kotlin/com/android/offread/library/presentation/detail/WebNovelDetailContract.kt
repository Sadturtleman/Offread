package com.android.offread.library.presentation.detail

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.library.domain.model.Chapter
import com.android.offread.library.domain.model.LibraryItem

data class WebNovelDetailUiState(
    val item: LibraryItem? = null,
    val chapters: List<Chapter> = emptyList(),
    val preparing: Boolean = false,
) : UiState

sealed interface WebNovelDetailIntent : MviIntent {
    data object PrepareOffline : WebNovelDetailIntent

    data object ContinueReading : WebNovelDetailIntent

    data object CheckForNewChapters : WebNovelDetailIntent
}

sealed interface WebNovelDetailEvent : ReducerEvent {
    data class ItemLoaded(
        val item: LibraryItem,
        val chapters: List<Chapter>,
    ) : WebNovelDetailEvent

    data class Preparing(
        val preparing: Boolean,
    ) : WebNovelDetailEvent
}

sealed interface WebNovelDetailEffect : MviEffect {
    data class ShowMessage(
        val message: String,
    ) : WebNovelDetailEffect

    /** 이어읽기 → 리더(R-01). */
    data class OpenReader(
        val itemId: String,
        val chapterIndex: Int,
    ) : WebNovelDetailEffect
}
