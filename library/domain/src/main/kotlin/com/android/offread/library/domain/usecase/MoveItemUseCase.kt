package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.TranslationCache
import com.android.offread.library.domain.model.TermMapMoveStrategy
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-007: 아이템을 다른 컬렉션으로 이동. 용어맵은 전략에 따라 처리하고,
 * 캐시 키에 collectionId 가 포함되므로 이동 후 아이템 번역 캐시를 무효화한다.
 */
class MoveItemUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val translationCache: TranslationCache,
    ) {
        suspend operator fun invoke(
            itemId: String,
            targetCollectionId: String,
            strategy: TermMapMoveStrategy,
        ): Result<Unit> {
            val item =
                libraryRepository.observeItem(itemId).first()
                    ?: return Result.failure(ItemNotFoundException)
            if (item.collectionId == targetCollectionId) return Result.failure(SameCollectionException)
            libraryRepository.moveItem(itemId, targetCollectionId, strategy)
            translationCache.invalidateItem(itemId)
            return Result.success(Unit)
        }
    }

/** 이동할 아이템이 없을 때. */
object ItemNotFoundException : IllegalArgumentException("아이템을 찾을 수 없어요.")

/** 대상이 현재 컬렉션과 같을 때. */
object SameCollectionException : IllegalArgumentException("이미 이 컬렉션에 있어요.")
