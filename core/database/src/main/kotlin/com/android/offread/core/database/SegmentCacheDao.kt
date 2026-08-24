package com.android.offread.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SegmentCacheDao {
    @Query(
        "SELECT translation FROM segment_cache " +
            "WHERE contentHash = :contentHash AND collectionId = :collectionId AND modelVersion = :modelVersion",
    )
    suspend fun get(
        contentHash: String,
        collectionId: String,
        modelVersion: String,
    ): String?

    @Upsert
    suspend fun upsert(entry: SegmentCacheEntity)

    @Query("DELETE FROM segment_cache WHERE itemId = :itemId")
    suspend fun deleteByItem(itemId: String)

    @Query("DELETE FROM segment_cache WHERE itemId = :itemId AND chapterIndex = :chapterIndex")
    suspend fun deleteByChapter(
        itemId: String,
        chapterIndex: Int,
    )

    @Query("DELETE FROM segment_cache WHERE collectionId = :collectionId")
    suspend fun deleteByCollection(collectionId: String)

    @Query("SELECT COUNT(*) FROM segment_cache")
    suspend fun entryCount(): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM segment_cache")
    suspend fun totalBytes(): Long

    /** F-031: 아이템별 캐시 사용량 집계. */
    @Query("SELECT itemId, COUNT(*) AS entryCount, COALESCE(SUM(sizeBytes), 0) AS bytes FROM segment_cache GROUP BY itemId")
    suspend fun usageByItem(): List<SegmentCacheUsage>

    @Query("DELETE FROM segment_cache")
    suspend fun clear()
}

/** 아이템별 캐시 집계 결과(F-031). */
data class SegmentCacheUsage(
    val itemId: String,
    val entryCount: Int,
    val bytes: Long,
)
