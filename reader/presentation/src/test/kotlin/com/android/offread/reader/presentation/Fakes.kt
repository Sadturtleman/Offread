package com.android.offread.reader.presentation

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.model.SearchQuery
import com.android.offread.library.domain.model.TermMapMoveStrategy
import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSegment
import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.model.SegmentCacheKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeChapterContentRepository : ChapterContentRepository {
    var getChapterCalls = 0

    override suspend fun getChapter(
        itemId: String,
        chapterIndex: Int,
    ): ChapterContent =
        ChapterContent(
            itemId = itemId,
            chapterIndex = chapterIndex,
            title = "${chapterIndex}화".also { getChapterCalls++ },
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

    override fun searchItems(query: SearchQuery): Flow<List<LibraryItem>> = items.map { list -> list.filter { query.matches(it) } }

    override fun observeItem(id: String): Flow<LibraryItem?> = items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addItem(item: LibraryItem): String = item.id

    override suspend fun moveItem(
        id: String,
        targetCollectionId: String,
        strategy: TermMapMoveStrategy,
    ) = Unit

    override suspend fun updateTotalChapters(
        id: String,
        totalChapters: Int,
    ) {
        items.value = items.value.map { if (it.id == id) it.copy(totalChapters = totalChapters) else it }
    }

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

/** 인메모리 [TermRepository] 더블(F-017 용어 빠른편집). */
class FakeTermRepository : TermRepository {
    private val terms = MutableStateFlow<List<Term>>(emptyList())

    override fun observeTerms(collectionId: String): Flow<List<Term>> =
        terms.map { list -> list.filter { it.collectionId == collectionId } }

    override suspend fun upsert(term: Term): String {
        val id = term.id.ifBlank { "t${terms.value.size}" }
        terms.value = terms.value + term.copy(id = id)
        return id
    }

    override suspend fun delete(id: String) {
        terms.value = terms.value.filterNot { it.id == id }
    }

    fun current(): List<Term> = terms.value
}

/** 무효화 호출을 기록하는 [SegmentCache] 더블. */
class FakeSegmentCache : SegmentCache {
    val invalidatedChapters = mutableListOf<Pair<String, Int>>()

    override suspend fun get(key: SegmentCacheKey): String? = null

    override suspend fun put(
        key: SegmentCacheKey,
        itemId: String,
        chapterIndex: Int,
        translation: String,
    ) = Unit

    override suspend fun invalidateItem(itemId: String) = Unit

    override suspend fun invalidateChapter(
        itemId: String,
        chapterIndex: Int,
    ) {
        invalidatedChapters += itemId to chapterIndex
    }

    override suspend fun invalidateCollection(collectionId: String) = Unit

    override suspend fun stats(): CacheStats = CacheStats.EMPTY

    override suspend fun usageByItem(): Map<String, CacheStats> = emptyMap()

    override suspend fun clear() = Unit
}
