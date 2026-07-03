package com.android.offread.onboarding.presentation.download

import androidx.lifecycle.viewModelScope
import com.android.offread.core.entity.TranslationModel
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import com.android.offread.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.android.offread.onboarding.domain.usecase.EnqueueModelDownloadsUseCase
import com.android.offread.onboarding.domain.usecase.ObserveModelDownloadsUseCase
import com.android.offread.onboarding.domain.usecase.PauseModelDownloadUseCase
import com.android.offread.onboarding.domain.usecase.ResolveRequiredModelsUseCase
import com.android.offread.onboarding.domain.usecase.ResumeModelDownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-003 번역 모델 다운로드. 진입 시 필요한 모델을 해석·큐잉하고 진행 상태를 구독한다.
 * 필요한 모델이 없거나 전부 완료되면 온보딩을 마치고 라이브러리로 이동한다.
 */
@HiltViewModel
class ModelDownloadViewModel
    @Inject
    constructor(
        private val resolveRequiredModels: ResolveRequiredModelsUseCase,
        private val enqueueModelDownloads: EnqueueModelDownloadsUseCase,
        private val observeModelDownloads: ObserveModelDownloadsUseCase,
        private val pauseModelDownload: PauseModelDownloadUseCase,
        private val resumeModelDownload: ResumeModelDownloadUseCase,
        private val completeOnboarding: CompleteOnboardingUseCase,
    ) : MviViewModel<ModelDownloadIntent, ModelDownloadUiState, ModelDownloadEvent, ModelDownloadEffect>(
            ModelDownloadUiState(),
        ) {
        private var models: List<TranslationModel> = emptyList()

        init {
            start()
        }

        private fun start() {
            viewModelScope.launch {
                models = resolveRequiredModels()
                if (models.isEmpty()) {
                    // 이미 설치돼 있으면 바로 첫 번역 체험으로.
                    emitEffect(ModelDownloadEffect.NavigateToFirstTranslation)
                    return@launch
                }
                enqueueModelDownloads(models)
                observeModelDownloads().collect { statuses ->
                    val items =
                        models.map { model ->
                            ModelDownloadItem(model, statuses[model.id] ?: ModelDownloadStatus.Queued)
                        }
                    dispatch(ModelDownloadEvent.ItemsChanged(items))
                    if (currentState.allCompleted) {
                        emitEffect(ModelDownloadEffect.NavigateToFirstTranslation)
                    }
                }
            }
        }

        override fun onIntent(intent: ModelDownloadIntent) {
            when (intent) {
                is ModelDownloadIntent.TogglePause -> togglePause(intent.modelId)
                ModelDownloadIntent.SkipForNow -> skip()
            }
        }

        private fun togglePause(modelId: String) {
            val item = currentState.items.firstOrNull { it.model.id == modelId } ?: return
            viewModelScope.launch {
                if (item.isPaused) resumeModelDownload(modelId) else pauseModelDownload(modelId)
            }
        }

        // '나중에 하기'는 모델 없이 온보딩을 마치고 라이브러리로. (완료 표시는 여기서.)
        private fun skip() {
            viewModelScope.launch {
                completeOnboarding()
                emitEffect(ModelDownloadEffect.NavigateToLibrary)
            }
        }

        override fun reduce(
            state: ModelDownloadUiState,
            event: ModelDownloadEvent,
        ): ModelDownloadUiState =
            when (event) {
                is ModelDownloadEvent.ItemsChanged -> state.copy(items = event.items)
            }
    }
