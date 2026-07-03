package com.android.offread.onboarding.presentation.intro

import androidx.lifecycle.viewModelScope
import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.onboarding.domain.usecase.SelectLanguagePairsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-002 온보딩 인트로·언어쌍 선택. 선택 가능한 언어쌍 중 첫 항목을 기본 선택한다(디자인 O-02).
 * 저장 성공 시 모델 다운로드(O-03)로 넘긴다 — 온보딩 완료 표시는 플로우 끝(다운로드/스킵)에서 한다.
 */
@HiltViewModel
class OnboardingIntroViewModel
    @Inject
    constructor(
        private val selectLanguagePairs: SelectLanguagePairsUseCase,
    ) : MviViewModel<OnboardingIntroIntent, OnboardingIntroUiState, OnboardingIntroEvent, OnboardingIntroEffect>(
            initialState(),
        ) {
        override fun onIntent(intent: OnboardingIntroIntent) {
            when (intent) {
                is OnboardingIntroIntent.TogglePair -> toggle(intent.pair)
                OnboardingIntroIntent.Proceed -> proceed()
            }
        }

        private fun toggle(pair: LanguagePair) {
            if (!pair.isSelectable) return
            val current = currentState.selected
            val next = if (pair in current) current - pair else current + pair
            dispatch(OnboardingIntroEvent.SelectionChanged(next))
        }

        private fun proceed() {
            if (!currentState.canProceed) return
            val pairs = currentState.selected
            viewModelScope.launch {
                dispatch(OnboardingIntroEvent.Submitting(true))
                selectLanguagePairs(pairs)
                    .onSuccess {
                        emitEffect(OnboardingIntroEffect.NavigateNext)
                    }.onFailure { error ->
                        dispatch(OnboardingIntroEvent.Submitting(false))
                        emitEffect(OnboardingIntroEffect.ShowError(error.message ?: "언어쌍 저장에 실패했습니다."))
                    }
            }
        }

        override fun reduce(
            state: OnboardingIntroUiState,
            event: OnboardingIntroEvent,
        ): OnboardingIntroUiState =
            when (event) {
                is OnboardingIntroEvent.SelectionChanged -> state.copy(selected = event.selected)
                is OnboardingIntroEvent.Submitting -> state.copy(submitting = event.submitting)
            }

        private companion object {
            fun initialState(): OnboardingIntroUiState {
                val options = LanguagePair.entries.map { LanguagePairOption(it, it.isSelectable) }
                val defaultSelected = LanguagePair.entries.firstOrNull { it.isSelectable }
                return OnboardingIntroUiState(
                    options = options,
                    selected = setOfNotNull(defaultSelected),
                )
            }
        }
    }
