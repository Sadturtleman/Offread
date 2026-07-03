package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair

/** 결정적 [TranslationEngine] 테스트 더블. */
class FakeTranslationEngine : TranslationEngine {
    override fun sampleText(pair: LanguagePair): String = "SAMPLE_${pair.name}"

    override suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String = "T[$text]"
}
