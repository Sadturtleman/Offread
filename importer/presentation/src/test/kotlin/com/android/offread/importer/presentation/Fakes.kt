package com.android.offread.importer.presentation

import com.android.offread.core.entity.SerialStatus
import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.importer.domain.model.WebNovelMetadata
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWebNovelImporter(
    private val supported: Boolean = true,
) : WebNovelImporter {
    override fun isSupported(url: String): Boolean = supported

    override suspend fun recognize(url: String): WebNovelMetadata =
        WebNovelMetadata("소설가가 되자", "무직전생", "손의 손", 286, SerialStatus.ONGOING, url)
}

class FakeLibraryRepository : LibraryRepository {
    private val collections = MutableStateFlow<List<Collection>>(emptyList())
    private val items = MutableStateFlow<List<LibraryItem>>(emptyList())
    private var next = 0

    override fun observeCollections(sort: LibrarySort): Flow<List<Collection>> = collections

    override suspend fun createCollection(
        name: String,
        parentId: String?,
    ): String {
        val id = "c${next++}"
        collections.value = collections.value + Collection(id, name, parentId, 0, 0, next.toLong())
        return id
    }

    override suspend fun renameCollection(
        id: String,
        name: String,
    ) = Unit

    override suspend fun deleteCollection(id: String) = Unit

    override fun observeItems(collectionId: String?): Flow<List<LibraryItem>> =
        items.map { list -> if (collectionId == null) list else list.filter { it.collectionId == collectionId } }

    override suspend fun addItem(item: LibraryItem): String {
        val id = "i${next++}"
        items.value = items.value + item.copy(id = id)
        return id
    }

    fun seedCollection(name: String): String {
        val id = "c${next++}"
        collections.value = collections.value + Collection(id, name, null, 0, 0, next.toLong())
        return id
    }

    fun currentItems(): List<LibraryItem> = items.value
}
