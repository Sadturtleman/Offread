package com.android.offread.settings.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.settings.domain.model.DownloadedContent
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.SegmentCache
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** F-031: 번역 캐시가 지금 얼마나 차지하는지. */
class GetCacheStatsUseCase
    @Inject
    constructor(
        private val segmentCache: SegmentCache,
    ) {
        suspend operator fun invoke(): CacheStats = segmentCache.stats()
    }

/**
 * F-031: 저장 공간을 쓰고 있는 콘텐츠 목록.
 * 아이템별 캐시 사용량은 캐시가 아이템 단위로 집계해 준다.
 */
class GetDownloadedContentsUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val segmentCache: SegmentCache,
    ) {
        suspend operator fun invoke(): List<DownloadedContent> {
            val usage = segmentCache.usageByItem()
            return libraryRepository
                .observeItems()
                .first()
                .mapNotNull { item ->
                    val itemUsage = usage[item.id] ?: return@mapNotNull null
                    DownloadedContent(item = item, cachedSegments = itemUsage.entryCount, bytes = itemUsage.bytes)
                }.sortedByDescending { it.bytes }
        }
    }

/** F-031: 번역 캐시 전체 비우기(P-04). 원문·모델은 건드리지 않는다. */
class ClearCacheUseCase
    @Inject
    constructor(
        private val segmentCache: SegmentCache,
    ) {
        suspend operator fun invoke() = segmentCache.clear()
    }

/** F-031: 특정 작품의 캐시만 비운다. */
class ClearItemCacheUseCase
    @Inject
    constructor(
        private val segmentCache: SegmentCache,
    ) {
        suspend operator fun invoke(itemId: String) = segmentCache.invalidateItem(itemId)
    }
