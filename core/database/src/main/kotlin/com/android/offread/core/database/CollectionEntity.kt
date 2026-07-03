package com.android.offread.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 컬렉션 테이블. 자기참조 FK(parentId → id, CASCADE)로 무제한 중첩과 삭제 연쇄(P-04)를 표현한다.
 * 컬렉션 삭제 시 하위 컬렉션이 DB 레벨에서 함께 삭제된다(하위 아이템·용어·캐시는 각 테이블 FK 로 연쇄 예정).
 */
@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId")],
)
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String?,
    val itemCount: Int,
    val termCount: Int,
    val updatedAt: Long,
)
