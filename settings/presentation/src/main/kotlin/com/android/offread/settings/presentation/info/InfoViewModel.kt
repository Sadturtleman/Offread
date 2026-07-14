package com.android.offread.settings.presentation.info

import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.core.ui.mvi.NoEffect
import com.android.offread.settings.domain.usecase.GetAppInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * F-032 정보. 컴포지션 루트(app)가 바인딩한 앱 메타를 노출한다.
 */
@HiltViewModel
class InfoViewModel
    @Inject
    constructor(
        getAppInfo: GetAppInfoUseCase,
    ) : MviViewModel<InfoIntent, InfoUiState, InfoEvent, NoEffect>(InfoUiState()) {
        init {
            dispatch(InfoEvent.AppInfoLoaded(getAppInfo()))
        }

        override fun onIntent(intent: InfoIntent) = Unit

        override fun reduce(
            state: InfoUiState,
            event: InfoEvent,
        ): InfoUiState =
            when (event) {
                is InfoEvent.AppInfoLoaded -> state.copy(appInfo = event.appInfo)
            }
    }
