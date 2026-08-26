package com.android.offread.translate.data

import com.android.offread.core.database.SegmentCacheDao
import com.android.offread.core.database.SegmentCacheEntity
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.model.SegmentCacheKey
import javax.inject.Inject

/** [SegmentCache] Room 어댑터. 캐시는 앱 전용 저장소에만 남는다. */
class RoomSegmentCache
    @Inject
    constructor(
        private val dao: SegmentCacheDao,
    ) : SegmentCache {
        override suspend fun get(key: SegmentCacheKey): String? = dao.get(key.contentHash, key.modelVersion)

        override suspend fun put(
            key: SegmentCacheKey,
            translation: String,
        ) {
            dao.upsert(
                SegmentCacheEntity(
                    contentHash = key.contentHash,
                    modelVersion = key.modelVersion,
                    translation = translation,
                    sizeBytes = translation.toByteArray(Charsets.UTF_8).size,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }

        override suspend fun stats(): CacheStats = CacheStats(entryCount = dao.entryCount(), bytes = dao.totalBytes())

        override suspend fun clear() = dao.clear()
    }
