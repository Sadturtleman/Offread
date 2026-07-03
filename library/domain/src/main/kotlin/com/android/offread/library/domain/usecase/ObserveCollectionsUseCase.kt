package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** F-005: 정렬 기준에 맞춰 컬렉션 목록을 구독한다. */
class ObserveCollectionsUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(sort: LibrarySort): Flow<List<Collection>> = libraryRepository.observeCollections(sort)
    }
