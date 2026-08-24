package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * F-010: 제목·작가로 아이템을 검색한다(전역 또는 컬렉션 내, 유형 필터).
 * 빈 질의는 저장소를 건드리지 않고 빈 결과를 돌려준다.
 */
class SearchItemsUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(query: SearchQuery): Flow<List<LibraryItem>> =
            if (query.isBlank) {
                flowOf(emptyList())
            } else {
                libraryRepository.searchItems(query.copy(text = query.text.trim()))
            }
    }
