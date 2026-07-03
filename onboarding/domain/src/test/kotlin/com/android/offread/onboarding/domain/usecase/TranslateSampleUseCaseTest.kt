package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.FakeTranslationEngine
import com.android.offread.onboarding.domain.model.TranslationSample
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslateSampleUseCaseTest {
    @Test
    fun `샘플 원문을 엔진으로 번역한다`() =
        runTest {
            val useCase = TranslateSampleUseCase(FakeTranslationEngine())
            val sample = TranslationSample(LanguagePair.JA_KO, "こんにちは")

            assertEquals("T[こんにちは]", useCase(sample))
        }
}
