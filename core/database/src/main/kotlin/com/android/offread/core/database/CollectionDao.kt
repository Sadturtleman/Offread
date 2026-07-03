package com.android.offread.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun observeByRecent(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections ORDER BY name COLLATE NOCASE ASC")
    fun observeByName(): Flow<List<CollectionEntity>>

    @Insert
    suspend fun insert(collection: CollectionEntity)

    @Query("UPDATE collections SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(
        id: String,
        name: String,
        updatedAt: Long,
    )

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: String)
}
