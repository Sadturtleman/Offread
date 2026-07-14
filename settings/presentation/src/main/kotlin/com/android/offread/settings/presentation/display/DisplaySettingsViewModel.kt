package com.android.offread.settings.presentation.display

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.usecase.ObserveDisplaySettingsUseCase
import com.android.offread.settings.domain.usecase.UpdateDisplaySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-030 표시 설정. 저장된 설정을 구독하고 변경 즉시 영속한다(P-01 온디바이스).
 */
@HiltViewModel
class DisplaySettingsViewModel
    @Inject
    constructor(
        observeDisplaySettings: ObserveDisplaySettingsUseCase,
        private val updateDisplaySettings: UpdateDisplaySettingsUseCase,
    ) : MviViewModel<DisplaySettingsIntent, DisplaySettingsUiState, DisplaySettingsEvent, DisplaySettingsEffect>(
            DisplaySettingsUiState(),
        ) {
        init {
            viewModelScope.launch {
                observeDisplaySettings().collect { settings ->
                    dispatch(DisplaySettingsEvent.SettingsChanged(settings))
                }
            }
        }

        override fun onIntent(intent: DisplaySettingsIntent) {
            when (intent) {
                is DisplaySettingsIntent.ChangeFontScale -> update { it.copy(fontScale = intent.fontScale) }
                is DisplaySettingsIntent.ChangeTheme -> update { it.copy(theme = intent.theme) }
                is DisplaySettingsIntent.ChangeTranslationDisplay -> update { it.copy(translationDisplay = intent.mode) }
            }
        }

        private fun update(transform: (DisplaySettings) -> DisplaySettings) {
            viewModelScope.launch {
                runCatching { updateDisplaySettings(transform(currentState.settings)) }
                    .onFailure { emitEffect(DisplaySettingsEffect.ShowError(it.message ?: "설정 저장에 실패했어요.")) }
            }
        }

        override fun reduce(
            state: DisplaySettingsUiState,
            event: DisplaySettingsEvent,
        ): DisplaySettingsUiState =
            when (event) {
                is DisplaySettingsEvent.SettingsChanged -> state.copy(settings = event.settings)
            }
    }
