package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.FakeOnboardingRepository
import com.android.offread.onboarding.domain.FakeTranslationModelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveRequiredModelsUseCaseTest {
    @Test
    fun `선택 언어쌍 중 미설치 모델만 반환한다`() =
        runTest {
            val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO))
            val models = FakeTranslationModelRepository(installed = setOf(LanguagePair.ZH_KO))
            val useCase = ResolveRequiredModelsUseCase(onboarding, models)

            val result = useCase()

            assertEquals(1, result.size)
            assertEquals(LanguagePair.JA_KO, result.first().languagePair)
        }

    @Test
    fun `모두 설치돼 있으면 빈 목록을 반환한다`() =
        runTest {
            val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
            val models = FakeTranslationModelRepository(installed = setOf(LanguagePair.JA_KO))
            val useCase = ResolveRequiredModelsUseCase(onboarding, models)

            assertTrue(useCase().isEmpty())
        }
}
