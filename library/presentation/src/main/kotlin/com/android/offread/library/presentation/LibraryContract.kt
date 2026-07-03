package com.android.offread.library.presentation

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibrarySort

/** 컬렉션 CRUD 다이얼로그 상태. */
sealed interface CollectionDialog {
    data object Create : CollectionDialog

    data class Rename(
        val collection: Collection,
    ) : CollectionDialog

    data class ConfirmDelete(
        val collection: Collection,
    ) : CollectionDialog
}

data class LibraryUiState(
    val collections: List<Collection> = emptyList(),
    val sort: LibrarySort = LibrarySort.RECENT,
    val dialog: CollectionDialog? = null,
    val loading: Boolean = true,
) : UiState {
    val isEmpty: Boolean get() = !loading && collections.isEmpty()
}

sealed interface LibraryIntent : MviIntent {
    data class ChangeSort(
        val sort: LibrarySort,
    ) : LibraryIntent

    data object AddCollectionClicked : LibraryIntent

    data class RenameCollectionClicked(
        val collection: Collection,
    ) : LibraryIntent

    data class DeleteCollectionClicked(
        val collection: Collection,
    ) : LibraryIntent

    data object DismissDialog : LibraryIntent

    data class SubmitCreate(
        val name: String,
    ) : LibraryIntent

    data class SubmitRename(
        val id: String,
        val name: String,
    ) : LibraryIntent

    data class ConfirmDelete(
        val id: String,
    ) : LibraryIntent

    data object ImportClicked : LibraryIntent
}

sealed interface LibraryEvent : ReducerEvent {
    data class CollectionsChanged(
        val collections: List<Collection>,
    ) : LibraryEvent

    data class SortChanged(
        val sort: LibrarySort,
    ) : LibraryEvent

    data class DialogChanged(
        val dialog: CollectionDialog?,
    ) : LibraryEvent
}

sealed interface LibraryEffect : MviEffect {
    data class ShowError(
        val message: String,
    ) : LibraryEffect

    /** 가져오기(I 그룹) 진입 — 아직 미구현이라 화면에서 안내로 대체. */
    data object NavigateToImport : LibraryEffect
}
