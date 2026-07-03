package com.android.offread.onboarding.presentation.splash

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState

/** 스플래시는 사용자 입력이 없다(자동 분기). */
sealed interface SplashIntent : MviIntent

data object SplashUiState : UiState

/** 스플래시는 상태 변이가 없다. */
sealed interface SplashEvent : ReducerEvent

sealed interface SplashEffect : MviEffect {
    /** 온보딩 미완료 → O-02. */
    data object NavigateToOnboarding : SplashEffect

    /** 온보딩 완료 → L-01(임시 Home). */
    data object NavigateToLibrary : SplashEffect
}
