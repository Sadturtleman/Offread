package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.FakeOnboardingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectLanguagePairsUseCaseTest {
    @Test
    fun `선택 가능한 언어쌍을 저장한다`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = SelectLanguagePairsUseCase(repo)

            val result = useCase(setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO))

            assertTrue(result.isSuccess)
            assertEquals(setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO), repo.selectedLanguagePairs.first())
        }

    @Test
    fun `하나도 선택하지 않으면 실패한다`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = SelectLanguagePairsUseCase(repo)

            val result = useCase(emptySet())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NoLanguagePairSelectedException)
            assertTrue(repo.selectedLanguagePairs.first().isEmpty())
        }

    @Test
    fun `제공 예정(COMING_SOON) 언어쌍을 고르면 실패하고 저장하지 않는다`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = SelectLanguagePairsUseCase(repo)

            val result = useCase(setOf(LanguagePair.JA_KO, LanguagePair.EN_KO))

            assertTrue(result.isFailure)
            val ex = result.exceptionOrNull()
            assertTrue(ex is UnavailableLanguagePairException)
            assertEquals(setOf(LanguagePair.EN_KO), (ex as UnavailableLanguagePairException).pairs)
            assertFalse(repo.selectedLanguagePairs.first().contains(LanguagePair.JA_KO))
        }
}
