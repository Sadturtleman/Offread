package com.android.offread.onboarding.presentation.splash

import com.android.offread.onboarding.domain.usecase.DetermineStartDestinationUseCase
import com.android.offread.onboarding.presentation.FakeOnboardingRepository
import com.android.offread.onboarding.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `온보딩 미완료면 온보딩으로 이동 이펙트를 낸다`() =
        runTest {
            val useCase = DetermineStartDestinationUseCase(FakeOnboardingRepository(initialComplete = false))
            val viewModel = SplashViewModel(useCase)

            assertEquals(SplashEffect.NavigateToOnboarding, viewModel.effect.first())
        }

    @Test
    fun `온보딩 완료면 라이브러리로 이동 이펙트를 낸다`() =
        runTest {
            val useCase = DetermineStartDestinationUseCase(FakeOnboardingRepository(initialComplete = true))
            val viewModel = SplashViewModel(useCase)

            assertEquals(SplashEffect.NavigateToLibrary, viewModel.effect.first())
        }
}
