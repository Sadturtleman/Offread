package com.android.offread.settings.presentation.storage

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.settings.domain.usecase.ClearCacheUseCase
import com.android.offread.settings.domain.usecase.ClearItemCacheUseCase
import com.android.offread.settings.domain.usecase.GetCacheStatsUseCase
import com.android.offread.settings.domain.usecase.GetDownloadedContentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * S-05 저장·캐시 관리(F-031). 캐시 용량을 보여주고 전체 또는 작품별로 비운다(P-04).
 * 캐시는 다시 만들 수 있는 데이터라 되돌릴 수 없다는 점만 확인받고 즉시 지운다.
 */
@HiltViewModel
class StorageSettingsViewModel
    @Inject
    constructor(
        private val getCacheStats: GetCacheStatsUseCase,
        private val getDownloadedContents: GetDownloadedContentsUseCase,
        private val clearCache: ClearCacheUseCase,
        private val clearItemCache: ClearItemCacheUseCase,
    ) : MviViewModel<StorageSettingsIntent, StorageSettingsUiState, StorageSettingsEvent, StorageSettingsEffect>(
            StorageSettingsUiState(),
        ) {
        init {
            refresh()
        }

        override fun onIntent(intent: StorageSettingsIntent) {
            when (intent) {
                StorageSettingsIntent.ClearAllClicked ->
                    dispatch(StorageSettingsEvent.ClearTargetChanged(ClearTarget.All))
                is StorageSettingsIntent.ClearItemClicked ->
                    dispatch(StorageSettingsEvent.ClearTargetChanged(ClearTarget.Item(intent.content)))
                StorageSettingsIntent.DismissClear -> dispatch(StorageSettingsEvent.ClearTargetChanged(null))
                StorageSettingsIntent.ConfirmClear -> confirmClear()
            }
        }

        private fun confirmClear() {
            val target = currentState.clearTarget ?: return
            viewModelScope.launch {
                val message =
                    when (target) {
                        ClearTarget.All -> {
                            clearCache()
                            "번역 캐시를 비웠어요."
                        }
                        is ClearTarget.Item -> {
                            clearItemCache(target.content.item.id)
                            "${target.content.item.title} 의 캐시를 비웠어요."
                        }
                    }
                dispatch(StorageSettingsEvent.ClearTargetChanged(null))
                loadStorage()
                emitEffect(StorageSettingsEffect.ShowMessage(message))
            }
        }

        private fun refresh() {
            viewModelScope.launch { loadStorage() }
        }

        private suspend fun loadStorage() {
            dispatch(StorageSettingsEvent.Loaded(getCacheStats(), getDownloadedContents()))
        }

        override fun reduce(
            state: StorageSettingsUiState,
            event: StorageSettingsEvent,
        ): StorageSettingsUiState =
            when (event) {
                is StorageSettingsEvent.Loaded -> state.copy(cache = event.cache, contents = event.contents)
                is StorageSettingsEvent.ClearTargetChanged -> state.copy(clearTarget = event.target)
            }
    }
