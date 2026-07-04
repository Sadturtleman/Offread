package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** F-008: 아이템 상세를 구독한다. */
class GetItemUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(id: String): Flow<LibraryItem?> = libraryRepository.observeItem(id)
    }
