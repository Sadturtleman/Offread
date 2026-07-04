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

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun delete(id: String)
}
