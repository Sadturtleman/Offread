package com.android.offread.terms.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.terms.domain.model.TermFilter
import com.android.offread.terms.domain.usecase.AcceptSuggestionUseCase
import com.android.offread.terms.domain.usecase.DeleteTermUseCase
import com.android.offread.terms.domain.usecase.ObserveTermsUseCase
import com.android.offread.terms.domain.usecase.RejectSuggestionUseCase
import com.android.offread.terms.domain.usecase.UpsertTermUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-025/F-026 용어맵. [start] 로 컬렉션 id 를 받아 필터별 용어를 구독하고 CRUD·제안 처리를 한다.
 */
@HiltViewModel
class TermMapViewModel
    @Inject
    constructor(
        private val observeTerms: ObserveTermsUseCase,
        private val upsertTerm: UpsertTermUseCase,
        private val deleteTerm: DeleteTermUseCase,
        private val acceptSuggestion: AcceptSuggestionUseCase,
        private val rejectSuggestion: RejectSuggestionUseCase,
    ) : MviViewModel<TermMapIntent, TermMapUiState, TermMapEvent, TermMapEffect>(TermMapUiState()) {
        private var collectionId: String = ""
        private val filterFlow = MutableStateFlow(TermFilter.ALL)
        private var started = false

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        fun start(collectionId: String) {
            if (started) return
            started = true
            this.collectionId = collectionId
            viewModelScope.launch {
                filterFlow
                    .flatMapLatest { filter -> observeTerms(collectionId, filter) }
                    .collect { terms -> dispatch(TermMapEvent.TermsChanged(terms)) }
            }
        }

        override fun onIntent(intent: TermMapIntent) {
            when (intent) {
                is TermMapIntent.ChangeFilter -> {
                    filterFlow.value = intent.filter
                    dispatch(TermMapEvent.FilterChanged(intent.filter))
                }
                TermMapIntent.AddClicked -> dispatch(TermMapEvent.DialogChanged(TermDialog.Add))
                is TermMapIntent.EditClicked -> dispatch(TermMapEvent.DialogChanged(TermDialog.Edit(intent.term)))
                TermMapIntent.DismissDialog -> dispatch(TermMapEvent.DialogChanged(null))
                is TermMapIntent.Submit -> submit(intent)
                is TermMapIntent.Delete ->
                    viewModelScope.launch {
                        deleteTerm(intent.id)
                        closeDialog()
                    }
                is TermMapIntent.AcceptSuggestion -> viewModelScope.launch { acceptSuggestion(intent.term) }
                is TermMapIntent.RejectSuggestion -> viewModelScope.launch { rejectSuggestion(intent.term) }
            }
        }

        private fun submit(intent: TermMapIntent.Submit) {
            val existing = (currentState.dialog as? TermDialog.Edit)?.term
            viewModelScope.launch {
                upsertTerm(
                    UpsertTermUseCase.Input(
                        collectionId = collectionId,
                        source = intent.source,
                        translation = intent.translation,
                        pinned = intent.pinned,
                        existing = existing,
                    ),
                ).onSuccess { closeDialog() }
                    .onFailure { emitEffect(TermMapEffect.ShowError(it.message ?: "저장에 실패했어요.")) }
            }
        }

        private fun closeDialog() = dispatch(TermMapEvent.DialogChanged(null))

        override fun reduce(
            state: TermMapUiState,
            event: TermMapEvent,
        ): TermMapUiState =
            when (event) {
                is TermMapEvent.TermsChanged -> state.copy(terms = event.terms)
                is TermMapEvent.FilterChanged -> state.copy(filter = event.filter)
                is TermMapEvent.DialogChanged -> state.copy(dialog = event.dialog)
            }
    }
