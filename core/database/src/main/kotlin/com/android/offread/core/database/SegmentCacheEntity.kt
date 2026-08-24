package com.android.offread.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 세그먼트 번역 캐시 테이블(F-021). 키는 콘텐츠 해시 + collectionId + 모델 버전이며,
 * collectionId FK(CASCADE)로 컬렉션 삭제 시 캐시도 함께 사라진다(P-04).
 * 원문은 저장하지 않고 해시만 남긴다.
 */
@Entity(
    tableName = "segment_cache",
    primaryKeys = ["contentHash", "collectionId", "modelVersion"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("collectionId"), Index("itemId"), Index(value = ["itemId", "chapterIndex"])],
)
data class SegmentCacheEntity(
    val contentHash: String,
    val collectionId: String,
    val modelVersion: String,
    val itemId: String,
    val chapterIndex: Int,
    val translation: String,
    val sizeBytes: Int,
    val updatedAt: Long,
)
