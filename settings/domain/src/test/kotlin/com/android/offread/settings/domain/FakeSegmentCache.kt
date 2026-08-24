package com.android.offread.settings.domain

import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.model.SegmentCacheKey

/** 아이템별 사용량을 직접 세팅하는 [SegmentCache] 더블(F-031 테스트용). */
class FakeSegmentCache(
    private val usage: MutableMap<String, CacheStats> = mutableMapOf(),
) : SegmentCache {
    var cleared = false
    val invalidatedItems = mutableListOf<String>()

    override suspend fun get(key: SegmentCacheKey): String? = null

    override suspend fun put(
        key: SegmentCacheKey,
        itemId: String,
        chapterIndex: Int,
        translation: String,
    ) = Unit

    override suspend fun invalidateItem(itemId: String) {
        invalidatedItems += itemId
        usage.remove(itemId)
    }

    override suspend fun invalidateChapter(
        itemId: String,
        chapterIndex: Int,
    ) = Unit

    override suspend fun invalidateCollection(collectionId: String) = Unit

    override suspend fun stats(): CacheStats =
        CacheStats(
            entryCount = usage.values.sumOf { it.entryCount },
            bytes = usage.values.sumOf { it.bytes },
        )

    override suspend fun usageByItem(): Map<String, CacheStats> = usage.toMap()

    override suspend fun clear() {
        cleared = true
        usage.clear()
    }
}
