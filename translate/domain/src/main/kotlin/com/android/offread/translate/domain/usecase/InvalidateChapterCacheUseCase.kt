package com.android.offread.translate.domain.usecase

import com.android.offread.translate.domain.SegmentCache
import javax.inject.Inject

/**
 * F-017: 용어를 고치고 현재 챕터를 다시 번역할 때, 그 챕터의 캐시를 버린다.
 * 캐시를 지우지 않으면 새 용어가 반영되지 않은 옛 번역이 그대로 나온다(F-021).
 */
class InvalidateChapterCacheUseCase
    @Inject
    constructor(
        private val cache: SegmentCache,
    ) {
        suspend operator fun invoke(
            itemId: String,
            chapterIndex: Int,
        ) = cache.invalidateChapter(itemId, chapterIndex)
    }
