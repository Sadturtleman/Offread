package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.FakeTranslationEngine
import com.android.offread.onboarding.domain.FakeTranslationModelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetSampleTextUseCaseTest {
    @Test
    fun `설치된 언어쌍의 샘플 원문을 반환한다`() =
        runTest {
            val models = FakeTranslationModelRepository(installed = setOf(LanguagePair.JA_KO))
            val useCase = GetSampleTextUseCase(models, FakeTranslationEngine())

            val sample = useCase()

            assertEquals(LanguagePair.JA_KO, sample?.languagePair)
            assertEquals("SAMPLE_JA_KO", sample?.originalText)
        }

    @Test
    fun `설치된 모델이 없으면 null 을 반환한다`() =
        runTest {
            val models = FakeTranslationModelRepository(installed = emptySet())
            val useCase = GetSampleTextUseCase(models, FakeTranslationEngine())

            assertNull(useCase())
        }
}
