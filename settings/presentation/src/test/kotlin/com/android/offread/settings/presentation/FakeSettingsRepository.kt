package com.android.offread.settings.presentation

import com.android.offread.settings.domain.SettingsRepository
import com.android.offread.settings.domain.model.DisplaySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(DisplaySettings())

    override val displaySettings: Flow<DisplaySettings> = state

    override suspend fun updateDisplaySettings(settings: DisplaySettings) {
        state.value = settings
    }

    fun seed(settings: DisplaySettings) {
        state.value = settings
    }
}
