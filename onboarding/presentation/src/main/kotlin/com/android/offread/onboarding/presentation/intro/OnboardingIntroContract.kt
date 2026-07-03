package com.android.offread.onboarding.presentation.intro

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState

/** 한 줄 언어쌍 옵션. [enabled] 가 false 면 제공 예정(COMING_SOON)이라 선택 불가. */
data class LanguagePairOption(
    val pair: LanguagePair,
    val enabled: Boolean,
)

data class OnboardingIntroUiState(
    val options: List<LanguagePairOption> = emptyList(),
    val selected: Set<LanguagePair> = emptySet(),
    val submitting: Boolean = false,
) : UiState {
    val canProceed: Boolean get() = selected.isNotEmpty() && !submitting
}

sealed interface OnboardingIntroIntent : MviIntent {
    data class TogglePair(
        val pair: LanguagePair,
    ) : OnboardingIntroIntent

    data object Proceed : OnboardingIntroIntent
}

sealed interface OnboardingIntroEvent : ReducerEvent {
    data class SelectionChanged(
        val selected: Set<LanguagePair>,
    ) : OnboardingIntroEvent

    data class Submitting(
        val submitting: Boolean,
    ) : OnboardingIntroEvent
}

sealed interface OnboardingIntroEffect : MviEffect {
    /** 언어쌍 저장 성공 → 모델 다운로드(O-03). */
    data object NavigateNext : OnboardingIntroEffect

    data class ShowError(
        val message: String,
    ) : OnboardingIntroEffect
}
