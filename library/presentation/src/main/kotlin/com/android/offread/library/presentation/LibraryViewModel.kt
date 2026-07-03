package com.android.offread.library.presentation

import androidx.lifecycle.viewModelScope
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.usecase.CreateCollectionUseCase
import com.android.offread.library.domain.usecase.DeleteCollectionUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.ObserveItemsUseCase
import com.android.offread.library.domain.usecase.RenameCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-005/F-006 라이브러리 홈. 정렬 변경에 따라 컬렉션 구독을 교체하고, CRUD 다이얼로그 흐름을 관리한다.
 */
@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        private val observeCollections: ObserveCollectionsUseCase,
        private val observeItems: ObserveItemsUseCase,
        private val createCollection: CreateCollectionUseCase,
        private val renameCollection: RenameCollectionUseCase,
        private val deleteCollection: DeleteCollectionUseCase,
    ) : MviViewModel<LibraryIntent, LibraryUiState, LibraryEvent, LibraryEffect>(LibraryUiState()) {
        private val sortFlow = MutableStateFlow(LibrarySort.RECENT)
        private var observeJob: Job? = null

        init {
            observeSorted()
            observeItems()
        }

        private fun observeItems() {
            viewModelScope.launch {
                observeItems.invoke().collect { items -> dispatch(LibraryEvent.ItemsChanged(items)) }
            }
        }

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        private fun observeSorted() {
            observeJob?.cancel()
            observeJob =
                viewModelScope.launch {
                    sortFlow
                        .flatMapLatest { sort -> observeCollections(sort) }
                        .collect { collections -> dispatch(LibraryEvent.CollectionsChanged(collections)) }
                }
        }

        override fun onIntent(intent: LibraryIntent) {
            when (intent) {
                is LibraryIntent.ChangeSort -> {
                    sortFlow.value = intent.sort
                    dispatch(LibraryEvent.SortChanged(intent.sort))
                }
                LibraryIntent.AddCollectionClicked -> dispatch(LibraryEvent.DialogChanged(CollectionDialog.Create))
                is LibraryIntent.RenameCollectionClicked ->
                    dispatch(LibraryEvent.DialogChanged(CollectionDialog.Rename(intent.collection)))
                is LibraryIntent.DeleteCollectionClicked ->
                    dispatch(LibraryEvent.DialogChanged(CollectionDialog.ConfirmDelete(intent.collection)))
                LibraryIntent.DismissDialog -> dispatch(LibraryEvent.DialogChanged(null))
                is LibraryIntent.SubmitCreate -> submitCreate(intent.name)
                is LibraryIntent.SubmitRename -> submitRename(intent.id, intent.name)
                is LibraryIntent.ConfirmDelete -> confirmDelete(intent.id)
                LibraryIntent.ImportClicked -> emitEffect(LibraryEffect.NavigateToImport)
            }
        }

        private fun submitCreate(name: String) {
            viewModelScope.launch {
                createCollection(name)
                    .onSuccess { dispatch(LibraryEvent.DialogChanged(null)) }
                    .onFailure { emitEffect(LibraryEffect.ShowError(it.message ?: "생성에 실패했습니다.")) }
            }
        }

        private fun submitRename(
            id: String,
            name: String,
        ) {
            viewModelScope.launch {
                renameCollection(id, name)
                    .onSuccess { dispatch(LibraryEvent.DialogChanged(null)) }
                    .onFailure { emitEffect(LibraryEffect.ShowError(it.message ?: "이름 변경에 실패했습니다.")) }
            }
        }

        private fun confirmDelete(id: String) {
            viewModelScope.launch {
                deleteCollection(id)
                dispatch(LibraryEvent.DialogChanged(null))
            }
        }

        override fun reduce(
            state: LibraryUiState,
            event: LibraryEvent,
        ): LibraryUiState =
            when (event) {
                is LibraryEvent.CollectionsChanged -> state.copy(collections = event.collections, loading = false)
                is LibraryEvent.ItemsChanged -> state.copy(items = event.items, loading = false)
                is LibraryEvent.SortChanged -> state.copy(sort = event.sort)
                is LibraryEvent.DialogChanged -> state.copy(dialog = event.dialog)
            }
    }
