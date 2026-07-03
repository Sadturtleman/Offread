package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.TranslationEngine
import com.android.offread.onboarding.domain.model.TranslationSample
import javax.inject.Inject

/** F-004: 샘플 원문을 온디바이스로 번역한다. */
class TranslateSampleUseCase
    @Inject
    constructor(
        private val translationEngine: TranslationEngine,
    ) {
        suspend operator fun invoke(sample: TranslationSample): String =
            translationEngine.translate(sample.originalText, sample.languagePair)
    }
