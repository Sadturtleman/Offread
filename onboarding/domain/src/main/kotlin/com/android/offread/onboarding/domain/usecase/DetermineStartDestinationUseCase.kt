package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.OnboardingRepository
import com.android.offread.onboarding.domain.StartDestination
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-001 최초 실행 판별. 로컬 플래그만으로 온보딩/라이브러리 진입을 분기한다(네트워크·서버 없음).
 */
class DetermineStartDestinationUseCase
    @Inject
    constructor(
        private val onboardingRepository: OnboardingRepository,
    ) {
        suspend operator fun invoke(): StartDestination =
            if (onboardingRepository.isOnboardingComplete.first()) {
                StartDestination.LIBRARY
            } else {
                StartDestination.ONBOARDING
            }
    }
