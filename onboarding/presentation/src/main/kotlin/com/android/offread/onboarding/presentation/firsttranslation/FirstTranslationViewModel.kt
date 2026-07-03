package com.android.offread.onboarding.presentation.firsttranslation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.android.offread.onboarding.domain.usecase.GetSampleTextUseCase
import com.android.offread.onboarding.domain.usecase.TranslateSampleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-004 첫 번역 체험. 진입 시 설치된 언어쌍의 샘플 원문을 불러오고(없으면 즉시 스킵),
 * '번역하기'로 온디바이스 번역을 시연한 뒤 '시작하기'에서 온보딩을 완료한다.
 */
@HiltViewModel
class FirstTranslationViewModel
    @Inject
    constructor(
        private val getSampleText: GetSampleTextUseCase,
        private val translateSample: TranslateSampleUseCase,
        private val completeOnboarding: CompleteOnboardingUseCase,
    ) : MviViewModel<FirstTranslationIntent, FirstTranslationUiState, FirstTranslationEvent, FirstTranslationEffect>(
            FirstTranslationUiState(),
        ) {
        init {
            loadSample()
        }

        private fun loadSample() {
            viewModelScope.launch {
                val sample = getSampleText()
                if (sample == null) {
                    // 설치된 모델이 없으면(‘나중에’ 경로) 체험을 건너뛰고 마무리한다.
                    finish()
                } else {
                    dispatch(FirstTranslationEvent.SampleLoaded(sample))
                }
            }
        }

        override fun onIntent(intent: FirstTranslationIntent) {
            when (intent) {
                FirstTranslationIntent.Translate -> translate()
                FirstTranslationIntent.Start -> viewModelScope.launch { finish() }
            }
        }

        private fun translate() {
            val sample = currentState.sample ?: return
            if (!currentState.canTranslate) return
            viewModelScope.launch {
                dispatch(FirstTranslationEvent.Translating(true))
                val translated = translateSample(sample)
                dispatch(FirstTranslationEvent.Translated(translated))
            }
        }

        private suspend fun finish() {
            completeOnboarding()
            emitEffect(FirstTranslationEffect.NavigateToLibrary)
        }

        override fun reduce(
            state: FirstTranslationUiState,
            event: FirstTranslationEvent,
        ): FirstTranslationUiState =
            when (event) {
                is FirstTranslationEvent.SampleLoaded -> state.copy(sample = event.sample)
                is FirstTranslationEvent.Translating -> state.copy(translating = event.translating)
                is FirstTranslationEvent.Translated -> state.copy(translated = event.text, translating = false)
            }
    }
