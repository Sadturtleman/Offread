package com.android.offread.onboarding.presentation

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.TranslationEngine

/** 결정적 [TranslationEngine] 더블(presentation 단위 테스트용). */
class FakeTranslationEngine : TranslationEngine {
    override fun sampleText(pair: LanguagePair): String = "SAMPLE_${pair.name}"

    override suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String = "T[$text]"
}
