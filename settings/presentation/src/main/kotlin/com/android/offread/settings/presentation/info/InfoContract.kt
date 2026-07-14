package com.android.offread.settings.presentation.info

import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.settings.domain.AppInfo

data class InfoUiState(
    val appInfo: AppInfo? = null,
) : UiState

sealed interface InfoIntent : MviIntent

sealed interface InfoEvent : ReducerEvent {
    data class AppInfoLoaded(
        val appInfo: AppInfo,
    ) : InfoEvent
}
