package com.android.offread.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TermDao {
    @Query("SELECT * FROM terms WHERE collectionId = :collectionId ORDER BY updatedAt DESC")
    fun observeByCollection(collectionId: String): Flow<List<TermEntity>>

    @Query("SELECT * FROM terms WHERE collectionId = :collectionId")
    suspend fun getByCollection(collectionId: String): List<TermEntity>

    @Upsert
    suspend fun upsert(term: TermEntity)

    @Query("DELETE FROM terms WHERE id = :id")
    suspend fun delete(id: String)

    /** 이동 전 중복 정리: 대상 컬렉션에 같은 원문이 있으면 원 컬렉션 용어를 버린다(대상 우선). */
    @Query(
        "DELETE FROM terms WHERE collectionId = :sourceCollectionId " +
            "AND source IN (SELECT source FROM terms WHERE collectionId = :targetCollectionId)",
    )
    suspend fun deleteDuplicatedBySource(
        sourceCollectionId: String,
        targetCollectionId: String,
    )

    @Query(
        "UPDATE terms SET collectionId = :targetCollectionId, updatedAt = :updatedAt " +
            "WHERE collectionId = :sourceCollectionId",
    )
    suspend fun moveAll(
        sourceCollectionId: String,
        targetCollectionId: String,
        updatedAt: Long,
    )
}
