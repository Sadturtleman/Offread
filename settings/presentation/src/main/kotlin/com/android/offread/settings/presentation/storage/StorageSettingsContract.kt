package com.android.offread.settings.presentation.storage

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.settings.domain.model.DownloadedContent
import com.android.offread.translate.domain.CacheStats

/** 비우기 확인 대상. 전체 비우기와 작품별 비우기를 같은 다이얼로그로 다룬다. */
sealed interface ClearTarget {
    data object All : ClearTarget

    data class Item(
        val content: DownloadedContent,
    ) : ClearTarget
}

data class StorageSettingsUiState(
    val cache: CacheStats = CacheStats.EMPTY,
    val contents: List<DownloadedContent> = emptyList(),
    val clearTarget: ClearTarget? = null,
) : UiState {
    val isEmpty: Boolean get() = contents.isEmpty()
}

sealed interface StorageSettingsIntent : MviIntent {
    data object ClearAllClicked : StorageSettingsIntent

    data class ClearItemClicked(
        val content: DownloadedContent,
    ) : StorageSettingsIntent

    data object DismissClear : StorageSettingsIntent

    data object ConfirmClear : StorageSettingsIntent
}

sealed interface StorageSettingsEvent : ReducerEvent {
    data class Loaded(
        val cache: CacheStats,
        val contents: List<DownloadedContent>,
    ) : StorageSettingsEvent

    data class ClearTargetChanged(
        val target: ClearTarget?,
    ) : StorageSettingsEvent
}

sealed interface StorageSettingsEffect : MviEffect {
    data class ShowMessage(
        val message: String,
    ) : StorageSettingsEffect
}
