package com.android.offread.settings.presentation.models

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.settings.domain.usecase.DeleteLlmModelUseCase
import com.android.offread.settings.domain.usecase.DeleteModelUseCase
import com.android.offread.settings.domain.usecase.DownloadModelUseCase
import com.android.offread.settings.domain.usecase.ImportLlmModelUseCase
import com.android.offread.settings.domain.usecase.ObserveLlmModelFilesUseCase
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
        private val observeLlmModelFiles: ObserveLlmModelFilesUseCase,
        private val importLlmModel: ImportLlmModelUseCase,
        private val deleteLlmModel: DeleteLlmModelUseCase,
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
            refreshLlmModels()
        }

        private fun refreshLlmModels() {
            viewModelScope.launch { dispatch(ModelSettingsEvent.LlmModelsChanged(observeLlmModelFiles())) }
        }

        override fun onIntent(intent: ModelSettingsIntent) {
            when (intent) {
                is ModelSettingsIntent.SelectEngine ->
                    viewModelScope.launch {
                        selectTranslationEngine(intent.kind)
                        emitEffect(ModelSettingsEffect.ShowMessage("다음 번역부터 새 엔진을 써요."))
                    }
                is ModelSettingsIntent.ImportLlmModel -> importModel(intent.uri)
                is ModelSettingsIntent.DeleteLlmModel ->
                    viewModelScope.launch {
                        deleteLlmModel(intent.name)
                        dispatch(ModelSettingsEvent.LlmModelsChanged(observeLlmModelFiles()))
                        emitEffect(ModelSettingsEffect.ShowMessage("모델 파일을 지웠어요."))
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

        /** 수 GB 복사라 진행 상태를 노출한다. */
        private fun importModel(uri: String) {
            if (currentState.importing) return
            viewModelScope.launch {
                dispatch(ModelSettingsEvent.Importing(true))
                importLlmModel(uri)
                    .onSuccess { file ->
                        dispatch(ModelSettingsEvent.LlmModelsChanged(observeLlmModelFiles()))
                        emitEffect(ModelSettingsEffect.ShowMessage("${file.name} 을 가져왔어요."))
                    }.onFailure {
                        emitEffect(ModelSettingsEffect.ShowMessage(it.message ?: "모델을 가져오지 못했어요."))
                    }
                dispatch(ModelSettingsEvent.Importing(false))
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
                is ModelSettingsEvent.LlmModelsChanged -> state.copy(llmModels = event.files)
                is ModelSettingsEvent.Importing -> state.copy(importing = event.importing)
                is ModelSettingsEvent.ModelsChanged -> state.copy(models = event.models)
                is ModelSettingsEvent.DeleteTargetChanged -> state.copy(deleteTarget = event.model)
            }
    }
