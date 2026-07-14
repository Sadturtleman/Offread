package com.android.offread.settings.domain.usecase

import com.android.offread.settings.domain.SettingsRepository
import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.model.DisplayTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(DisplaySettings())
    override val displaySettings: Flow<DisplaySettings> = state

    override suspend fun updateDisplaySettings(settings: DisplaySettings) {
        state.value = settings
    }
}

class DisplaySettingsUseCasesTest {
    @Test
    fun `표시 설정을 저장하고 다시 읽는다`() =
        runTest {
            val repo = FakeSettingsRepository()
            UpdateDisplaySettingsUseCase(repo)(DisplaySettings(fontScale = 1.2f, theme = DisplayTheme.SEPIA))

            val loaded = ObserveDisplaySettingsUseCase(repo)().first()

            assertEquals(1.2f, loaded.fontScale, 0.001f)
            assertEquals(DisplayTheme.SEPIA, loaded.theme)
        }

    @Test
    fun `폰트 배율은 허용 범위로 제한된다`() =
        runTest {
            val repo = FakeSettingsRepository()
            UpdateDisplaySettingsUseCase(repo)(DisplaySettings(fontScale = 9.0f))

            assertEquals(1.6f, ObserveDisplaySettingsUseCase(repo)().first().fontScale, 0.001f)
        }
}
