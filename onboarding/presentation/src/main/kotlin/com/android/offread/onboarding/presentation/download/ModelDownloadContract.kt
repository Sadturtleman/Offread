package com.android.offread.onboarding.presentation.download

import com.android.offread.core.entity.TranslationModel
import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.onboarding.domain.model.ModelDownloadStatus

/** 화면에 표시할 모델 1개의 다운로드 항목. */
data class ModelDownloadItem(
    val model: TranslationModel,
    val status: ModelDownloadStatus,
) {
    val isCompleted: Boolean get() = status is ModelDownloadStatus.Completed
    val isPaused: Boolean get() = status is ModelDownloadStatus.Paused
}

data class ModelDownloadUiState(
    val items: List<ModelDownloadItem> = emptyList(),
) : UiState {
    val total: Int get() = items.size
    val completedCount: Int get() = items.count { it.isCompleted }

    /** 다운로드할 모델이 있고 전부 완료됐는가. */
    val allCompleted: Boolean get() = items.isNotEmpty() && items.all { it.isCompleted }
}

sealed interface ModelDownloadIntent : MviIntent {
    data class TogglePause(
        val modelId: String,
    ) : ModelDownloadIntent

    /** '나중에 하기' — 모델 없이 온보딩을 마치고 라이브러리로. */
    data object SkipForNow : ModelDownloadIntent
}

sealed interface ModelDownloadEvent : ReducerEvent {
    data class ItemsChanged(
        val items: List<ModelDownloadItem>,
    ) : ModelDownloadEvent
}

sealed interface ModelDownloadEffect : MviEffect {
    /** 전부 완료 또는 '나중에 하기' → 라이브러리(임시 Home). F-004 붙으면 첫 번역 체험으로 바뀐다. */
    data object NavigateToLibrary : ModelDownloadEffect
}
