package com.android.offread.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 라이브러리 아이템 테이블. collectionId FK(CASCADE)로 컬렉션 삭제 시 함께 삭제된다(P-04).
 * enum 은 name 문자열로 저장한다.
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("collectionId")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val type: String,
    val title: String,
    val author: String,
    val sourceUrl: String,
    val siteName: String,
    val totalChapters: Int,
    val serialStatus: String,
    val translationStatus: String,
    val updatedAt: Long,
    val lastReadChapter: Int,
)
