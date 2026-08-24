package com.android.offread.settings.domain

import com.android.offread.settings.domain.model.DisplaySettings
import kotlinx.coroutines.flow.Flow

/**
 * 설정 저장 포트(헥사고날). 구현체는 DataStore 어댑터(settings:data). 온디바이스(P-01).
 */
interface SettingsRepository {
    val displaySettings: Flow<DisplaySettings>

    suspend fun updateDisplaySettings(settings: DisplaySettings)
}
