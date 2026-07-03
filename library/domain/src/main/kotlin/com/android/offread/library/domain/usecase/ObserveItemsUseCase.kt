package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** F-005: 라이브러리 아이템 목록을 구독한다(전역 또는 컬렉션 내). */
class ObserveItemsUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(collectionId: String? = null): Flow<List<LibraryItem>> = libraryRepository.observeItems(collectionId)
    }
