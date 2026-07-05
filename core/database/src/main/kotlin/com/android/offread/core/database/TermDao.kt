package com.android.offread.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TermDao {
    @Query("SELECT * FROM terms WHERE collectionId = :collectionId ORDER BY updatedAt DESC")
    fun observeByCollection(collectionId: String): Flow<List<TermEntity>>

    @Upsert
    suspend fun upsert(term: TermEntity)

    @Query("DELETE FROM terms WHERE id = :id")
    suspend fun delete(id: String)
}
