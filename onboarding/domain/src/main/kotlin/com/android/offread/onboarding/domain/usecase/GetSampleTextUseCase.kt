package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.TranslationEngine
import com.android.offread.onboarding.domain.TranslationModelRepository
import com.android.offread.onboarding.domain.model.TranslationSample
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-004: 설치된 첫 언어쌍의 내장 샘플 원문을 가져온다.
 * 설치된 모델이 없으면(‘나중에’ 경로) null → 첫 번역 체험을 건너뛴다.
 */
class GetSampleTextUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
        private val translationEngine: TranslationEngine,
    ) {
        suspend operator fun invoke(): TranslationSample? {
            val pair = translationModelRepository.installedLanguagePairs.first().firstOrNull() ?: return null
            return TranslationSample(pair, translationEngine.sampleText(pair))
        }
    }
