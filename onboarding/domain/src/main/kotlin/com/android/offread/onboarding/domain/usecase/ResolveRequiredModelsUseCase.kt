package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.OnboardingRepository
import com.android.offread.onboarding.domain.TranslationModelRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-003: 선택한 언어쌍 중 아직 설치되지 않은 모델만 골라 다운로드 대상 목록을 만든다.
 */
class ResolveRequiredModelsUseCase
    @Inject
    constructor(
        private val onboardingRepository: OnboardingRepository,
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(): List<TranslationModel> {
            val selected = onboardingRepository.selectedLanguagePairs.first()
            val installed = translationModelRepository.installedLanguagePairs.first()
            val needed = selected - installed
            return translationModelRepository.catalogFor(needed)
        }
    }
