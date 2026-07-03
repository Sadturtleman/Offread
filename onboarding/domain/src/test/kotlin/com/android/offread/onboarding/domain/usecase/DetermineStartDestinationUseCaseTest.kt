package com.android.offread.onboarding.domain.usecase

import com.android.offread.onboarding.domain.FakeOnboardingRepository
import com.android.offread.onboarding.domain.StartDestination
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DetermineStartDestinationUseCaseTest {
    @Test
    fun `온보딩 미완료면 ONBOARDING 으로 분기한다`() =
        runTest {
            val useCase = DetermineStartDestinationUseCase(FakeOnboardingRepository(initialComplete = false))

            assertEquals(StartDestination.ONBOARDING, useCase())
        }

    @Test
    fun `온보딩 완료면 LIBRARY 로 분기한다`() =
        runTest {
            val useCase = DetermineStartDestinationUseCase(FakeOnboardingRepository(initialComplete = true))

            assertEquals(StartDestination.LIBRARY, useCase())
        }
}
