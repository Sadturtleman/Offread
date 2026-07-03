package com.android.offread.onboarding.presentation.intro

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.usecase.SelectLanguagePairsUseCase
import com.android.offread.onboarding.presentation.FakeOnboardingRepository
import com.android.offread.onboarding.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingIntroViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeOnboardingRepository = FakeOnboardingRepository()) =
        OnboardingIntroViewModel(SelectLanguagePairsUseCase(repo))

    @Test
    fun `초기 상태는 선택 가능한 첫 언어쌍이 선택되고 제공예정은 비활성`() {
        val state = viewModel().uiState.value

        assertEquals(setOf(LanguagePair.JA_KO), state.selected)
        assertTrue(state.options.first { it.pair == LanguagePair.JA_KO }.enabled)
        assertFalse(state.options.first { it.pair == LanguagePair.EN_KO }.enabled)
        assertTrue(state.canProceed)
    }

    @Test
    fun `선택 가능한 언어쌍은 토글로 추가·해제된다`() {
        val vm = viewModel()

        vm.onIntent(OnboardingIntroIntent.TogglePair(LanguagePair.ZH_KO))
        assertEquals(setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO), vm.uiState.value.selected)

        vm.onIntent(OnboardingIntroIntent.TogglePair(LanguagePair.JA_KO))
        assertEquals(setOf(LanguagePair.ZH_KO), vm.uiState.value.selected)
    }

    @Test
    fun `제공예정 언어쌍 토글은 무시된다`() {
        val vm = viewModel()

        vm.onIntent(OnboardingIntroIntent.TogglePair(LanguagePair.EN_KO))

        assertFalse(
            vm.uiState.value.selected
                .contains(LanguagePair.EN_KO),
        )
    }

    @Test
    fun `다음을 누르면 언어쌍을 저장하고 다음(모델 다운로드) 이펙트를 낸다`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val vm = viewModel(repo)

            vm.onIntent(OnboardingIntroIntent.Proceed)

            assertEquals(OnboardingIntroEffect.NavigateNext, vm.effect.first())
            assertEquals(setOf(LanguagePair.JA_KO), repo.selectedLanguagePairs.first())
            // 온보딩 완료 표시는 다운로드/스킵 단계에서 하므로 이 시점엔 아직 미완료.
            assertFalse(repo.isOnboardingComplete.first())
        }
}
