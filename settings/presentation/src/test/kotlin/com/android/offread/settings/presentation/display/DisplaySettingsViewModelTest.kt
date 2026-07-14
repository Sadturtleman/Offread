package com.android.offread.settings.presentation.display

import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.model.DisplayTheme
import com.android.offread.settings.domain.model.TranslationDisplayMode
import com.android.offread.settings.domain.usecase.ObserveDisplaySettingsUseCase
import com.android.offread.settings.domain.usecase.UpdateDisplaySettingsUseCase
import com.android.offread.settings.presentation.FakeSettingsRepository
import com.android.offread.settings.presentation.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DisplaySettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeSettingsRepository): DisplaySettingsViewModel =
        DisplaySettingsViewModel(
            ObserveDisplaySettingsUseCase(repo),
            UpdateDisplaySettingsUseCase(repo),
        )

    @Test
    fun `저장된 표시 설정을 구독해 노출한다`() {
        val repo =
            FakeSettingsRepository().apply {
                seed(DisplaySettings(fontScale = 1.2f, theme = DisplayTheme.DARK))
            }

        val vm = viewModel(repo)

        assertEquals(1.2f, vm.uiState.value.settings.fontScale, 0.001f)
        assertEquals(DisplayTheme.DARK, vm.uiState.value.settings.theme)
    }

    @Test
    fun `폰트 크기 변경이 상태와 저장소에 반영된다`() {
        val repo = FakeSettingsRepository()
        val vm = viewModel(repo)

        vm.onIntent(DisplaySettingsIntent.ChangeFontScale(1.4f))

        assertEquals(1.4f, vm.uiState.value.settings.fontScale, 0.001f)
    }

    @Test
    fun `폰트 배율은 허용 범위로 제한된다`() {
        val repo = FakeSettingsRepository()
        val vm = viewModel(repo)

        vm.onIntent(DisplaySettingsIntent.ChangeFontScale(9.0f))

        assertEquals(DisplaySettings.MAX_FONT_SCALE, vm.uiState.value.settings.fontScale, 0.001f)
    }

    @Test
    fun `테마 변경이 반영된다`() {
        val repo = FakeSettingsRepository()
        val vm = viewModel(repo)

        vm.onIntent(DisplaySettingsIntent.ChangeTheme(DisplayTheme.SEPIA))

        assertEquals(DisplayTheme.SEPIA, vm.uiState.value.settings.theme)
    }

    @Test
    fun `번역 표시 방식 변경이 반영된다`() {
        val repo = FakeSettingsRepository()
        val vm = viewModel(repo)

        vm.onIntent(DisplaySettingsIntent.ChangeTranslationDisplay(TranslationDisplayMode.BILINGUAL))

        assertEquals(TranslationDisplayMode.BILINGUAL, vm.uiState.value.settings.translationDisplay)
    }

    @Test
    fun `연속 변경은 마지막 값으로 수렴한다`() {
        val repo = FakeSettingsRepository()
        val vm = viewModel(repo)

        vm.onIntent(DisplaySettingsIntent.ChangeTheme(DisplayTheme.DARK))
        vm.onIntent(DisplaySettingsIntent.ChangeFontScale(1.1f))

        assertEquals(DisplayTheme.DARK, vm.uiState.value.settings.theme)
        assertEquals(1.1f, vm.uiState.value.settings.fontScale, 0.001f)
    }
}
