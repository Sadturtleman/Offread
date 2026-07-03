package com.android.offread.onboarding.presentation.firsttranslation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.onboarding.domain.model.TranslationSample

data class FirstTranslationUiState(
    val sample: TranslationSample? = null,
    val translated: String? = null,
    val translating: Boolean = false,
) : UiState {
    val canTranslate: Boolean get() = sample != null && translated == null && !translating
}

sealed interface FirstTranslationIntent : MviIntent {
    data object Translate : FirstTranslationIntent

    /** '오프리드 시작하기' — 온보딩을 마치고 라이브러리로. */
    data object Start : FirstTranslationIntent
}

sealed interface FirstTranslationEvent : ReducerEvent {
    data class SampleLoaded(
        val sample: TranslationSample,
    ) : FirstTranslationEvent

    data class Translating(
        val translating: Boolean,
    ) : FirstTranslationEvent

    data class Translated(
        val text: String,
    ) : FirstTranslationEvent
}

sealed interface FirstTranslationEffect : MviEffect {
    data object NavigateToLibrary : FirstTranslationEffect
}
