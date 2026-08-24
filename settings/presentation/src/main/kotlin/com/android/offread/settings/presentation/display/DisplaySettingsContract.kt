package com.android.offread.settings.presentation.display

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.model.DisplayTheme
import com.android.offread.settings.domain.model.TranslationDisplayMode

data class DisplaySettingsUiState(
    val settings: DisplaySettings = DisplaySettings(),
) : UiState

sealed interface DisplaySettingsIntent : MviIntent {
    data class ChangeFontScale(
        val fontScale: Float,
    ) : DisplaySettingsIntent

    data class ChangeTheme(
        val theme: DisplayTheme,
    ) : DisplaySettingsIntent

    data class ChangeTranslationDisplay(
        val mode: TranslationDisplayMode,
    ) : DisplaySettingsIntent
}

sealed interface DisplaySettingsEvent : ReducerEvent {
    data class SettingsChanged(
        val settings: DisplaySettings,
    ) : DisplaySettingsEvent
}

sealed interface DisplaySettingsEffect : MviEffect {
    data class ShowError(
        val message: String,
    ) : DisplaySettingsEffect
}
