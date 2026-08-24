package com.android.offread.settings.presentation.models

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.settings.domain.usecase.DeleteModelUseCase
import com.android.offread.settings.domain.usecase.DownloadModelUseCase
import com.android.offread.settings.domain.usecase.ObserveManagedModelsUseCase
import com.android.offread.settings.domain.usecase.ObserveTranslationEngineUseCase
import com.android.offread.settings.domain.usecase.SelectTranslationEngineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * S-02 엔진·모델 관리(F-029). 언어쌍별 모델을 내려받고 지운다.
 * 클라우드 폴백 opt-in·API 키 입력은 Phase 2(F-023)에서 이 화면에 붙는다.
 */
@HiltViewModel
class ModelSettingsViewModel
    @Inject
    constructor(
        private val observeManagedModels: ObserveManagedModelsUseCase,
        private val downloadModel: DownloadModelUseCase,
        private val deleteModel: DeleteModelUseCase,
        private val observeTranslationEngine: ObserveTranslationEngineUseCase,
        private val selectTranslationEngine: SelectTranslationEngineUseCase,
    ) : MviViewModel<ModelSettingsIntent, ModelSettingsUiState, ModelSettingsEvent, ModelSettingsEffect>(
            ModelSettingsUiState(),
        ) {
        init {
            viewModelScope.launch {
                observeManagedModels().collect { models ->
                    dispatch(ModelSettingsEvent.ModelsChanged(models))
                }
            }
            viewModelScope.launch {
                observeTranslationEngine().collect { kind ->
                    dispatch(ModelSettingsEvent.EngineChanged(kind))
                }
            }
        }

        override fun onIntent(intent: ModelSettingsIntent) {
            when (intent) {
                is ModelSettingsIntent.SelectEngine ->
                    viewModelScope.launch {
                        selectTranslationEngine(intent.kind)
                        emitEffect(ModelSettingsEffect.ShowMessage("다음 번역부터 새 엔진을 써요."))
                    }
                is ModelSettingsIntent.Download ->
                    viewModelScope.launch {
                        downloadModel(intent.model.model)
                        emitEffect(ModelSettingsEffect.ShowMessage("${intent.model.model.displayName} 다운로드를 시작했어요."))
                    }
                is ModelSettingsIntent.DeleteClicked -> dispatch(ModelSettingsEvent.DeleteTargetChanged(intent.model))
                ModelSettingsIntent.DismissDelete -> dispatch(ModelSettingsEvent.DeleteTargetChanged(null))
                ModelSettingsIntent.ConfirmDelete -> confirmDelete()
            }
        }

        private fun confirmDelete() {
            val target = currentState.deleteTarget ?: return
            viewModelScope.launch {
                deleteModel(target.model.id)
                dispatch(ModelSettingsEvent.DeleteTargetChanged(null))
                emitEffect(ModelSettingsEffect.ShowMessage("모델을 지웠어요. 다시 쓰려면 내려받아야 해요."))
            }
        }

        override fun reduce(
            state: ModelSettingsUiState,
            event: ModelSettingsEvent,
        ): ModelSettingsUiState =
            when (event) {
                is ModelSettingsEvent.EngineChanged -> state.copy(engine = event.kind)
                is ModelSettingsEvent.ModelsChanged -> state.copy(models = event.models)
                is ModelSettingsEvent.DeleteTargetChanged -> state.copy(deleteTarget = event.model)
            }
    }
