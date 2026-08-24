package com.android.offread.translate.data

import com.android.offread.core.database.SegmentCacheDao
import com.android.offread.core.database.SegmentCacheEntity
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.model.SegmentCacheKey
import javax.inject.Inject

/**
 * [SegmentCache] Room 어댑터(F-021). 캐시는 앱 전용 저장소에만 남는다(P-01).
 */
class RoomSegmentCache
    @Inject
    constructor(
        private val dao: SegmentCacheDao,
    ) : SegmentCache {
        override suspend fun get(key: SegmentCacheKey): String? = dao.get(key.contentHash, key.collectionId, key.modelVersion)

        override suspend fun put(
            key: SegmentCacheKey,
            itemId: String,
            chapterIndex: Int,
            translation: String,
        ) {
            dao.upsert(
                SegmentCacheEntity(
                    contentHash = key.contentHash,
                    collectionId = key.collectionId,
                    modelVersion = key.modelVersion,
                    itemId = itemId,
                    chapterIndex = chapterIndex,
                    translation = translation,
                    sizeBytes = translation.toByteArray(Charsets.UTF_8).size,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }

        override suspend fun invalidateItem(itemId: String) = dao.deleteByItem(itemId)

        override suspend fun invalidateChapter(
            itemId: String,
            chapterIndex: Int,
        ) = dao.deleteByChapter(itemId, chapterIndex)

        override suspend fun invalidateCollection(collectionId: String) = dao.deleteByCollection(collectionId)

        override suspend fun stats(): CacheStats = CacheStats(entryCount = dao.entryCount(), bytes = dao.totalBytes())

        override suspend fun usageByItem(): Map<String, CacheStats> =
            dao.usageByItem().associate { it.itemId to CacheStats(entryCount = it.entryCount, bytes = it.bytes) }

        override suspend fun clear() = dao.clear()
    }
