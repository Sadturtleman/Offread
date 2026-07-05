package com.android.offread.terms.presentation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermFilter

/** 용어 편집 다이얼로그 상태(추가 또는 수정). */
sealed interface TermDialog {
    data object Add : TermDialog

    data class Edit(
        val term: Term,
    ) : TermDialog
}

data class TermMapUiState(
    val terms: List<Term> = emptyList(),
    val filter: TermFilter = TermFilter.ALL,
    val dialog: TermDialog? = null,
) : UiState

sealed interface TermMapIntent : MviIntent {
    data class ChangeFilter(
        val filter: TermFilter,
    ) : TermMapIntent

    data object AddClicked : TermMapIntent

    data class EditClicked(
        val term: Term,
    ) : TermMapIntent

    data object DismissDialog : TermMapIntent

    data class Submit(
        val source: String,
        val translation: String,
        val pinned: Boolean,
    ) : TermMapIntent

    data class Delete(
        val id: String,
    ) : TermMapIntent

    data class AcceptSuggestion(
        val term: Term,
    ) : TermMapIntent

    data class RejectSuggestion(
        val term: Term,
    ) : TermMapIntent
}

sealed interface TermMapEvent : ReducerEvent {
    data class TermsChanged(
        val terms: List<Term>,
    ) : TermMapEvent

    data class FilterChanged(
        val filter: TermFilter,
    ) : TermMapEvent

    data class DialogChanged(
        val dialog: TermDialog?,
    ) : TermMapEvent
}

sealed interface TermMapEffect : MviEffect {
    data class ShowError(
        val message: String,
    ) : TermMapEffect
}
