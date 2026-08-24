package com.android.offread.settings.presentation.models

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.settings.domain.model.ManagedModel
import com.android.offread.translate.domain.model.TranslationEngineKind

data class ModelSettingsUiState(
    val engine: TranslationEngineKind = TranslationEngineKind.ML_KIT,
    val models: List<ManagedModel> = emptyList(),
    /** 삭제 확인 대상(모델 id). */
    val deleteTarget: ManagedModel? = null,
) : UiState {
    /** 설치된 모델이 차지하는 용량 합계. */
    val installedBytes: Long get() = models.filter { it.installed }.sumOf { it.model.sizeBytes }
}

sealed interface ModelSettingsIntent : MviIntent {
    data class SelectEngine(
        val kind: TranslationEngineKind,
    ) : ModelSettingsIntent

    data class Download(
        val model: ManagedModel,
    ) : ModelSettingsIntent

    data class DeleteClicked(
        val model: ManagedModel,
    ) : ModelSettingsIntent

    data object DismissDelete : ModelSettingsIntent

    data object ConfirmDelete : ModelSettingsIntent
}

sealed interface ModelSettingsEvent : ReducerEvent {
    data class EngineChanged(
        val kind: TranslationEngineKind,
    ) : ModelSettingsEvent

    data class ModelsChanged(
        val models: List<ManagedModel>,
    ) : ModelSettingsEvent

    data class DeleteTargetChanged(
        val model: ManagedModel?,
    ) : ModelSettingsEvent
}

sealed interface ModelSettingsEffect : MviEffect {
    data class ShowMessage(
        val message: String,
    ) : ModelSettingsEffect
}
