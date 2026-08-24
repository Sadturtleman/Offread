package com.android.offread.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE collectionId = :collectionId ORDER BY updatedAt DESC")
    fun observeByCollection(collectionId: String): Flow<List<ItemEntity>>

    /**
     * F-010 검색. [text] 는 호출자가 LIKE 와일드카드를 이스케이프해 넘긴다.
     * [collectionId]·[type] 이 null 이면 해당 필터를 적용하지 않는다.
     */
    @Query(
        "SELECT * FROM items " +
            "WHERE (title LIKE '%' || :text || '%' ESCAPE '\\' " +
            "OR author LIKE '%' || :text || '%' ESCAPE '\\') " +
            "AND (:collectionId IS NULL OR collectionId = :collectionId) " +
            "AND (:type IS NULL OR type = :type) " +
            "ORDER BY updatedAt DESC",
    )
    fun search(
        text: String,
        collectionId: String?,
        type: String?,
    ): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun observeById(id: String): Flow<ItemEntity?>

    @Insert
    suspend fun insert(item: ItemEntity)

    @Query("UPDATE items SET translationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTranslationStatus(
        id: String,
        status: String,
        updatedAt: Long,
    )

    @Query("UPDATE items SET lastReadChapter = :chapter, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReadingProgress(
        id: String,
        chapter: Int,
        updatedAt: Long,
    )

    @Query("UPDATE items SET collectionId = :collectionId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCollection(
        id: String,
        collectionId: String,
        updatedAt: Long,
    )

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun delete(id: String)
}
