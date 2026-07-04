package com.android.offread.library.domain

import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 인메모리 [LibraryRepository] 테스트 더블. 정렬을 실제로 적용한다. */
class FakeLibraryRepository : LibraryRepository {
    private val collections = MutableStateFlow<List<Collection>>(emptyList())
    private var nextId = 0
    var nextTimestamp = 1000L

    override fun observeCollections(sort: LibrarySort): Flow<List<Collection>> =
        collections.map { list ->
            when (sort) {
                LibrarySort.RECENT -> list.sortedByDescending { it.updatedAt }
                LibrarySort.NAME -> list.sortedBy { it.name.lowercase() }
            }
        }

    override suspend fun createCollection(
        name: String,
        parentId: String?,
    ): String {
        val id = "c${nextId++}"
        collections.value =
            collections.value +
            Collection(id, name, parentId, itemCount = 0, termCount = 0, updatedAt = nextTimestamp++)
        return id
    }

    override suspend fun renameCollection(
        id: String,
        name: String,
    ) {
        collections.value = collections.value.map { if (it.id == id) it.copy(name = name) else it }
    }

    override suspend fun deleteCollection(id: String) {
        collections.value = collections.value.filterNot { it.id == id || it.parentId == id }
    }

    private val items = MutableStateFlow<List<LibraryItem>>(emptyList())

    override fun observeItems(collectionId: String?): Flow<List<LibraryItem>> =
        items.map { list -> if (collectionId == null) list else list.filter { it.collectionId == collectionId } }

    override fun observeItem(id: String): Flow<LibraryItem?> = items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addItem(item: LibraryItem): String {
        val id = "i${nextId++}"
        items.value = items.value + item.copy(id = id)
        return id
    }

    override suspend fun updateItemTranslationStatus(
        id: String,
        status: TranslationStatus,
    ) {
        items.value = items.value.map { if (it.id == id) it.copy(translationStatus = status) else it }
    }

    override suspend fun updateReadingProgress(
        id: String,
        lastReadChapter: Int,
    ) {
        items.value = items.value.map { if (it.id == id) it.copy(lastReadChapter = lastReadChapter) else it }
    }

    fun current(): List<Collection> = collections.value

    fun currentItems(): List<LibraryItem> = items.value

    fun seedItem(item: LibraryItem) {
        items.value = items.value + item
    }
}
