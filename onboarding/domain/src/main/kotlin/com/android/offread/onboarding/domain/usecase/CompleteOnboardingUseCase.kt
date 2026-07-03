package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.OnboardingRepository
import javax.inject.Inject

/**
 * 온보딩 플로우 완료 표시. 다음 실행부터 [DetermineStartDestinationUseCase] 가 라이브러리로 분기한다.
 *
 * NOTE: Phase 1 현재는 언어쌍 선택 직후 호출한다. 모델 다운로드(F-003)·첫 번역 체험(F-004)이
 * 추가되면 완료 시점을 플로우 끝으로 옮긴다.
 */
class CompleteOnboardingUseCase
    @Inject
    constructor(
        private val onboardingRepository: OnboardingRepository,
    ) {
        suspend operator fun invoke() {
            onboardingRepository.setOnboardingComplete(true)
        }
    }
