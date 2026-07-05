package com.android.offread.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 용어맵 테이블(컬렉션 스코프, F-024~F-026). collectionId FK(CASCADE)로 컬렉션 삭제 시 함께 삭제된다(P-04).
 */
@Entity(
    tableName = "terms",
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
data class TermEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val source: String,
    val translation: String,
    val pinned: Boolean,
    val origin: String,
    val occurrenceCount: Int,
    val status: String,
    val updatedAt: Long,
)
