package com.android.offread.core.database

import androidx.room.Entity

/**
 * 세그먼트 번역 캐시 테이블. 키는 콘텐츠 해시 + 모델 버전이고, 원문은 저장하지 않는다.
 */
@Entity(tableName = "segment_cache", primaryKeys = ["contentHash", "modelVersion"])
data class SegmentCacheEntity(
    val contentHash: String,
    val modelVersion: String,
    val translation: String,
    val sizeBytes: Int,
    val updatedAt: Long,
)
