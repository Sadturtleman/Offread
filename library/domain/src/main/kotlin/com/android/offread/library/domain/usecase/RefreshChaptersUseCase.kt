package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.ChapterSource
import com.android.offread.library.domain.LibraryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-013: 아이템 상세에서 수동 새로고침으로 신규 화를 확인한다.
 * 새 화가 있으면 화수를 갱신하고 늘어난 개수를 돌려준다(없으면 0).
 */
class RefreshChaptersUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val chapterSource: ChapterSource,
    ) {
        suspend operator fun invoke(itemId: String): Result<Int> {
            val item =
                libraryRepository.observeItem(itemId).first()
                    ?: return Result.failure(ItemNotFoundException)
            val latest =
                runCatching { chapterSource.fetchTotalChapters(item.sourceUrl) }
                    .getOrElse { return Result.failure(it) }
            // 사이트에서 화가 줄어 보이는 경우(삭제·일시 비공개)는 목록을 깎지 않는다.
            val added = latest - item.totalChapters
            if (added <= 0) return Result.success(0)
            libraryRepository.updateTotalChapters(itemId, latest)
            return Result.success(added)
        }
    }
