package com.android.offread.onboarding.presentation.splash

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.onboarding.domain.StartDestination
import com.android.offread.onboarding.domain.usecase.DetermineStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-001 최초 실행 판별. 생성 즉시 목적지를 계산해 1회성 네비게이션 이펙트를 방출한다(로컬 플래그, 1초 이내).
 */
@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val determineStartDestination: DetermineStartDestinationUseCase,
    ) : MviViewModel<SplashIntent, SplashUiState, SplashEvent, SplashEffect>(SplashUiState) {
        init {
            resolveDestination()
        }

        private fun resolveDestination() {
            viewModelScope.launch {
                val effect =
                    when (determineStartDestination()) {
                        StartDestination.ONBOARDING -> SplashEffect.NavigateToOnboarding
                        StartDestination.LIBRARY -> SplashEffect.NavigateToLibrary
                    }
                emitEffect(effect)
            }
        }

        override fun onIntent(intent: SplashIntent) = Unit

        override fun reduce(
            state: SplashUiState,
            event: SplashEvent,
        ): SplashUiState = state
    }
