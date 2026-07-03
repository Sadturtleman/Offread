package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.FakeOnboardingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteOnboardingUseCaseTest {
    @Test
    fun `온보딩 완료 플래그를 저장한다`() =
        runTest {
            val repo = FakeOnboardingRepository(initialComplete = false)
            val useCase = CompleteOnboardingUseCase(repo)

            useCase()

            assertTrue(repo.isOnboardingComplete.first())
        }
}
