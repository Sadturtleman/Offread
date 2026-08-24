package com.android.offread.settings.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.settings.domain.FakeLibraryRepository
import com.android.offread.settings.domain.FakeSegmentCache
import com.android.offread.translate.domain.CacheStats
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageUseCasesTest {
    private val cache =
        FakeSegmentCache(
            mutableMapOf(
                "i1" to CacheStats(entryCount = 10, bytes = 1_000),
                "i2" to CacheStats(entryCount = 3, bytes = 5_000),
            ),
        )
    private val library = FakeLibraryRepository()

    private fun item(
        id: String,
        title: String,
    ) = LibraryItem(
        id = id,
        collectionId = "c0",
        type = ItemType.WEBNOVEL,
        title = title,
        author = "작가",
        sourceUrl = "https://ncode.syosetu.com/$id/",
        siteName = "소설가가 되자",
        totalChapters = 10,
        serialStatus = SerialStatus.ONGOING,
        translationStatus = TranslationStatus.CACHED,
        updatedAt = 0,
    )

    @Test
    fun `캐시 총량을 돌려준다`() =
        runTest {
            val stats = GetCacheStatsUseCase(cache)()

            assertEquals(13, stats.entryCount)
            assertEquals(6_000, stats.bytes)
        }

    @Test
    fun `캐시를 쓰는 작품만 용량 큰 순으로 보여준다`() =
        runTest {
            library.seedItem(item("i1", "무직전생"))
            library.seedItem(item("i2", "전생 슬라임"))
            library.seedItem(item("i3", "캐시 없는 작품"))

            val contents = GetDownloadedContentsUseCase(library, cache)()

            assertEquals(listOf("i2", "i1"), contents.map { it.item.id })
            assertEquals(3, contents.first().cachedSegments)
        }

    @Test
    fun `전체 비우기는 캐시를 지운다`() =
        runTest {
            ClearCacheUseCase(cache)()

            assertTrue(cache.cleared)
            assertEquals(CacheStats.EMPTY, GetCacheStatsUseCase(cache)())
        }

    @Test
    fun `작품별 비우기는 그 작품만 지운다`() =
        runTest {
            library.seedItem(item("i1", "무직전생"))
            library.seedItem(item("i2", "전생 슬라임"))

            ClearItemCacheUseCase(cache)("i1")

            assertEquals(listOf("i1"), cache.invalidatedItems)
            assertEquals(listOf("i2"), GetDownloadedContentsUseCase(library, cache)().map { it.item.id })
        }
}
