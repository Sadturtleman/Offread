package com.android.offread.settings.domain.usecase

import com.android.offread.settings.domain.SettingsRepository
import com.android.offread.settings.domain.model.DisplaySettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** F-030: 표시 설정 구독. */
class ObserveDisplaySettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        operator fun invoke(): Flow<DisplaySettings> = settingsRepository.displaySettings
    }

/** F-030: 표시 설정 저장. 폰트 배율은 허용 범위로 제한한다. */
class UpdateDisplaySettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(settings: DisplaySettings) {
            val clamped =
                settings.copy(
                    fontScale =
                        settings.fontScale.coerceIn(
                            DisplaySettings.MIN_FONT_SCALE,
                            DisplaySettings.MAX_FONT_SCALE,
                        ),
                )
            settingsRepository.updateDisplaySettings(clamped)
        }
    }
