package com.android.offread.library.data

import com.android.offread.library.domain.TranslationCache
import com.android.offread.translate.domain.SegmentCache
import javax.inject.Inject

/**
 * 라이브러리의 [TranslationCache] 포트를 실제 세그먼트 캐시(F-021)에 연결하는 어댑터.
 * 캐시 키에 collectionId 가 들어가므로 아이템이 컬렉션을 옮기면 기존 캐시는 못 쓴다(F-007).
 */
class SegmentTranslationCache
    @Inject
    constructor(
        private val segmentCache: SegmentCache,
    ) : TranslationCache {
        override suspend fun invalidateItem(itemId: String) = segmentCache.invalidateItem(itemId)
    }
