package com.android.offread.library.presentation.search

import androidx.lifecycle.viewModelScope
import com.android.offread.core.entity.ItemType
import com.android.offread.core.ui.mvi.MviViewModel
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.model.SearchQuery
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.SearchItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F-010 검색. [start] 로 초기 컬렉션 스코프를 받는다(null 이면 전역).
 * 입력은 디바운스 후 로컬 Room 검색으로 흘린다.
 */
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val searchItems: SearchItemsUseCase,
        private val observeCollections: ObserveCollectionsUseCase,
    ) : MviViewModel<SearchIntent, SearchUiState, SearchEvent, SearchEffect>(SearchUiState()) {
        private val queryFlow = MutableStateFlow(SearchQuery())
        private var started = false

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        fun start(collectionId: String?) {
            if (started) return
            started = true
            queryFlow.value = SearchQuery(collectionId = collectionId)
            dispatch(SearchEvent.CollectionFilterChanged(collectionId))
            viewModelScope.launch {
                queryFlow
                    .debounce(DEBOUNCE_MILLIS)
                    .flatMapLatest { query -> searchItems(query).map { results -> query to results } }
                    .collect { (query, results) ->
                        dispatch(SearchEvent.ResultsChanged(results, searched = !query.isBlank))
                    }
            }
            viewModelScope.launch {
                observeCollections(LibrarySort.NAME).collect { collections ->
                    dispatch(SearchEvent.CollectionsChanged(collections))
                }
            }
        }

        override fun onIntent(intent: SearchIntent) {
            when (intent) {
                is SearchIntent.ChangeText -> changeText(intent.text)
                SearchIntent.ClearText -> changeText("")
                is SearchIntent.ChangeCollection -> {
                    queryFlow.update { it.copy(collectionId = intent.collectionId) }
                    dispatch(SearchEvent.CollectionFilterChanged(intent.collectionId))
                }
                is SearchIntent.ChangeType -> {
                    queryFlow.update { it.copy(type = intent.type) }
                    dispatch(SearchEvent.TypeFilterChanged(intent.type))
                }
            }
        }

        private fun changeText(text: String) {
            queryFlow.update { it.copy(text = text) }
            dispatch(SearchEvent.TextChanged(text))
        }

        override fun reduce(
            state: SearchUiState,
            event: SearchEvent,
        ): SearchUiState =
            when (event) {
                is SearchEvent.TextChanged -> state.copy(text = event.text)
                is SearchEvent.CollectionFilterChanged -> state.copy(collectionId = event.collectionId)
                is SearchEvent.TypeFilterChanged -> state.copy(type = event.type)
                is SearchEvent.CollectionsChanged -> state.copy(collections = event.collections)
                is SearchEvent.ResultsChanged -> state.copy(results = event.results, searched = event.searched)
            }

        private companion object {
            const val DEBOUNCE_MILLIS = 250L
        }
    }

/** 필터 칩 라벨. */
internal fun ItemType?.label(): String =
    when (this) {
        null -> "전체"
        ItemType.WEBNOVEL -> "웹소설"
        ItemType.PAPER -> "논문"
    }
