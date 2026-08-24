package com.android.offread.library.presentation.search

import com.android.offread.core.entity.ItemType
import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem

data class SearchUiState(
    val text: String = "",
    val collectionId: String? = null,
    val type: ItemType? = null,
    val collections: List<Collection> = emptyList(),
    val results: List<LibraryItem> = emptyList(),
    /** 질의를 실제로 던진 뒤인지. 빈 화면과 '결과 없음'을 구분한다. */
    val searched: Boolean = false,
) : UiState

sealed interface SearchIntent : MviIntent {
    data class ChangeText(
        val text: String,
    ) : SearchIntent

    data class ChangeCollection(
        val collectionId: String?,
    ) : SearchIntent

    data class ChangeType(
        val type: ItemType?,
    ) : SearchIntent

    data object ClearText : SearchIntent
}

sealed interface SearchEvent : ReducerEvent {
    data class TextChanged(
        val text: String,
    ) : SearchEvent

    data class CollectionFilterChanged(
        val collectionId: String?,
    ) : SearchEvent

    data class TypeFilterChanged(
        val type: ItemType?,
    ) : SearchEvent

    data class CollectionsChanged(
        val collections: List<Collection>,
    ) : SearchEvent

    data class ResultsChanged(
        val results: List<LibraryItem>,
        val searched: Boolean,
    ) : SearchEvent
}

/** 검색 화면은 결과 탭 시 화면 이동만 하므로 별도 이펙트가 없다. */
sealed interface SearchEffect : MviEffect
