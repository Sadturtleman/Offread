package com.android.offread.onboarding.presentation.firsttranslation

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.android.offread.onboarding.domain.usecase.GetSampleTextUseCase
import com.android.offread.onboarding.domain.usecase.TranslateSampleUseCase
import com.android.offread.onboarding.presentation.FakeOnboardingRepository
import com.android.offread.onboarding.presentation.FakeTranslationEngine
import com.android.offread.onboarding.presentation.FakeTranslationModelRepository
import com.android.offread.onboarding.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FirstTranslationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        onboarding: FakeOnboardingRepository = FakeOnboardingRepository(),
        installed: Set<LanguagePair> = setOf(LanguagePair.JA_KO),
    ): FirstTranslationViewModel {
        val models = FakeTranslationModelRepository(installed = installed)
        val engine = FakeTranslationEngine()
        return FirstTranslationViewModel(
            GetSampleTextUseCase(models, engine),
            TranslateSampleUseCase(engine),
            CompleteOnboardingUseCase(onboarding),
        )
    }

    @Test
    fun `진입 시 설치된 언어쌍의 샘플을 노출한다`() {
        val state = viewModel().uiState.value

        assertEquals(LanguagePair.JA_KO, state.sample?.languagePair)
        assertEquals("SAMPLE_JA_KO", state.sample?.originalText)
        assertTrue(state.canTranslate)
    }

    @Test
    fun `번역하기를 누르면 번역 결과를 노출한다`() {
        val vm = viewModel()

        vm.onIntent(FirstTranslationIntent.Translate)

        assertEquals("T[SAMPLE_JA_KO]", vm.uiState.value.translated)
        assertTrue(!vm.uiState.value.canTranslate)
    }

    @Test
    fun `시작하기를 누르면 온보딩 완료 후 라이브러리로 이동한다`() =
        runTest {
            val onboarding = FakeOnboardingRepository()
            val vm = viewModel(onboarding = onboarding)

            vm.onIntent(FirstTranslationIntent.Start)

            assertEquals(FirstTranslationEffect.NavigateToLibrary, vm.effect.first())
            assertTrue(onboarding.isOnboardingComplete.first())
        }

    @Test
    fun `설치된 모델이 없으면 즉시 온보딩을 마치고 라이브러리로 이동한다`() =
        runTest {
            val onboarding = FakeOnboardingRepository()
            val vm = viewModel(onboarding = onboarding, installed = emptySet())

            assertEquals(FirstTranslationEffect.NavigateToLibrary, vm.effect.first())
            assertTrue(onboarding.isOnboardingComplete.first())
        }
}
