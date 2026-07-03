package com.android.offread.onboarding.presentation.download

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import com.android.offread.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.android.offread.onboarding.domain.usecase.EnqueueModelDownloadsUseCase
import com.android.offread.onboarding.domain.usecase.ObserveModelDownloadsUseCase
import com.android.offread.onboarding.domain.usecase.PauseModelDownloadUseCase
import com.android.offread.onboarding.domain.usecase.ResolveRequiredModelsUseCase
import com.android.offread.onboarding.domain.usecase.ResumeModelDownloadUseCase
import com.android.offread.onboarding.presentation.FakeOnboardingRepository
import com.android.offread.onboarding.presentation.FakeTranslationModelRepository
import com.android.offread.onboarding.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ModelDownloadViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        onboarding: FakeOnboardingRepository,
        models: FakeTranslationModelRepository,
    ) = ModelDownloadViewModel(
        ResolveRequiredModelsUseCase(onboarding, models),
        EnqueueModelDownloadsUseCase(models),
        ObserveModelDownloadsUseCase(models),
        PauseModelDownloadUseCase(models),
        ResumeModelDownloadUseCase(models),
        CompleteOnboardingUseCase(onboarding),
    )

    @Test
    fun `필요한 모델을 큐잉하고 진행 항목을 노출한다`() {
        val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
        val models = FakeTranslationModelRepository()
        val vm = viewModel(onboarding, models)

        assertEquals(1, vm.uiState.value.total)
        assertEquals(1, models.enqueued.size)
        assertEquals("model-ja_ko", models.enqueued.first().id)
    }

    @Test
    fun `모든 모델 완료 시 온보딩 완료 후 라이브러리로 이동한다`() =
        runTest {
            val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
            val models = FakeTranslationModelRepository()
            val vm = viewModel(onboarding, models)

            models.emitDownloads(mapOf("model-ja_ko" to ModelDownloadStatus.Completed))

            assertEquals(ModelDownloadEffect.NavigateToLibrary, vm.effect.first())
            assertTrue(onboarding.isOnboardingComplete.first())
        }

    @Test
    fun `필요한 모델이 없으면 즉시 온보딩을 마친다`() =
        runTest {
            val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
            val models = FakeTranslationModelRepository(installed = setOf(LanguagePair.JA_KO))
            val vm = viewModel(onboarding, models)

            assertEquals(ModelDownloadEffect.NavigateToLibrary, vm.effect.first())
            assertTrue(onboarding.isOnboardingComplete.first())
            assertTrue(models.enqueued.isEmpty())
        }

    @Test
    fun `나중에 하기는 모델 없이 온보딩을 마친다`() =
        runTest {
            val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
            val models = FakeTranslationModelRepository()
            val vm = viewModel(onboarding, models)

            vm.onIntent(ModelDownloadIntent.SkipForNow)

            assertEquals(ModelDownloadEffect.NavigateToLibrary, vm.effect.first())
            assertTrue(onboarding.isOnboardingComplete.first())
        }

    @Test
    fun `일시정지 토글은 진행 중이면 pause 를 호출한다`() {
        val onboarding = FakeOnboardingRepository(initialPairs = setOf(LanguagePair.JA_KO))
        val models = FakeTranslationModelRepository()
        val vm = viewModel(onboarding, models)

        vm.onIntent(ModelDownloadIntent.TogglePause("model-ja_ko"))

        assertTrue(models.paused.contains("model-ja_ko"))
    }
}
