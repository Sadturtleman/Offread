package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.SearchQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchItemsUseCaseTest {
    private val repo = FakeLibraryRepository()
    private val searchItems = SearchItemsUseCase(repo)

    private fun item(
        id: String,
        title: String,
        author: String,
        collectionId: String = "c0",
        type: ItemType = ItemType.WEBNOVEL,
    ) = LibraryItem(
        id = id,
        collectionId = collectionId,
        type = type,
        title = title,
        author = author,
        sourceUrl = "https://ncode.syosetu.com/$id/",
        siteName = "소설가가 되자",
        totalChapters = 10,
        serialStatus = SerialStatus.ONGOING,
        translationStatus = TranslationStatus.UNTRANSLATED,
        updatedAt = 0,
    )

    private fun seed() {
        repo.seedItem(item("i1", "무직전생", "손의 손"))
        repo.seedItem(item("i2", "전생했더니 슬라임", "후세", collectionId = "c1"))
        repo.seedItem(item("i3", "Attention Is All You Need", "Vaswani", type = ItemType.PAPER))
    }

    @Test
    fun `빈 질의는 저장소를 조회하지 않고 빈 결과를 낸다`() =
        runTest {
            seed()

            assertTrue(searchItems(SearchQuery("")).first().isEmpty())
            assertTrue(searchItems(SearchQuery("   ")).first().isEmpty())
        }

    @Test
    fun `제목 부분 일치로 찾는다`() =
        runTest {
            seed()

            val results = searchItems(SearchQuery("전생")).first()

            assertEquals(listOf("i1", "i2"), results.map { it.id }.sorted())
        }

    @Test
    fun `작가로도 찾고 대소문자는 무시한다`() =
        runTest {
            seed()

            assertEquals(listOf("i3"), searchItems(SearchQuery("vaswani")).first().map { it.id })
        }

    @Test
    fun `앞뒤 공백은 제거하고 검색한다`() =
        runTest {
            seed()

            assertEquals(listOf("i1"), searchItems(SearchQuery("  무직  ")).first().map { it.id })
        }

    @Test
    fun `컬렉션 스코프를 주면 해당 컬렉션만 찾는다`() =
        runTest {
            seed()

            val results = searchItems(SearchQuery("전생", collectionId = "c1")).first()

            assertEquals(listOf("i2"), results.map { it.id })
        }

    @Test
    fun `유형 필터를 적용한다`() =
        runTest {
            seed()

            val papers = searchItems(SearchQuery("a", type = ItemType.PAPER)).first()

            assertEquals(listOf("i3"), papers.map { it.id })
            assertTrue(searchItems(SearchQuery("무직", type = ItemType.PAPER)).first().isEmpty())
        }
}
