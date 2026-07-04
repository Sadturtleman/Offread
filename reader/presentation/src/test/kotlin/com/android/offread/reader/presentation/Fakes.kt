package com.android.offread.reader.presentation

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeChapterContentRepository : ChapterContentRepository {
    override suspend fun getChapter(
        itemId: String,
        chapterIndex: Int,
    ): ChapterContent =
        ChapterContent(
            itemId = itemId,
            chapterIndex = chapterIndex,
            title = "${chapterIndex}화",
            segments =
                listOf(
                    ReaderSegment("s1", "原文1", "번역1"),
                    ReaderSegment("s2", "原文2", null),
                ),
        )

    override suspend fun retrySegment(
        itemId: String,
        chapterIndex: Int,
        segmentId: String,
    ): String = "재번역-$segmentId"
}

class FakeLibraryRepository(
    item: LibraryItem,
) : LibraryRepository {
    private val items = MutableStateFlow(listOf(item))

    override fun observeCollections(sort: LibrarySort): Flow<List<Collection>> = MutableStateFlow(emptyList())

    override suspend fun createCollection(
        name: String,
        parentId: String?,
    ): String = ""

    override suspend fun renameCollection(
        id: String,
        name: String,
    ) = Unit

    override suspend fun deleteCollection(id: String) = Unit

    override fun observeItems(collectionId: String?): Flow<List<LibraryItem>> = items

    override fun observeItem(id: String): Flow<LibraryItem?> = items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addItem(item: LibraryItem): String = item.id

    override suspend fun updateItemTranslationStatus(
        id: String,
        status: TranslationStatus,
    ) = Unit

    override suspend fun updateReadingProgress(
        id: String,
        lastReadChapter: Int,
    ) {
        items.value = items.value.map { if (it.id == id) it.copy(lastReadChapter = lastReadChapter) else it }
    }

    fun current(id: String): LibraryItem? = items.value.firstOrNull { it.id == id }
}

fun testItem(
    id: String = "i0",
    totalChapters: Int = 286,
) = LibraryItem(
    id = id,
    collectionId = "c0",
    type = ItemType.WEBNOVEL,
    title = "무직전생",
    author = "손의 손",
    sourceUrl = "https://ncode.syosetu.com/n9669bk/",
    siteName = "소설가가 되자",
    totalChapters = totalChapters,
    serialStatus = SerialStatus.ONGOING,
    translationStatus = TranslationStatus.CACHED,
    updatedAt = 0,
    lastReadChapter = 0,
)
