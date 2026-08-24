package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.TranslationCache
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.TermMapMoveStrategy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTranslationCache : TranslationCache {
    val invalidatedItemIds = mutableListOf<String>()

    override suspend fun invalidateItem(itemId: String) {
        invalidatedItemIds += itemId
    }
}

class MoveItemUseCaseTest {
    private fun item(collectionId: String = "c0") =
        LibraryItem(
            id = "i0",
            collectionId = collectionId,
            type = ItemType.WEBNOVEL,
            title = "무직전생",
            author = "손의 손",
            sourceUrl = "https://ncode.syosetu.com/n9669bk/",
            siteName = "소설가가 되자",
            totalChapters = 5,
            serialStatus = SerialStatus.ONGOING,
            translationStatus = TranslationStatus.UNTRANSLATED,
            updatedAt = 0,
        )

    @Test
    fun `이동하면 아이템 컬렉션이 바뀌고 캐시가 무효화된다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item()) }
            val cache = FakeTranslationCache()

            val result = MoveItemUseCase(repo, cache)("i0", "c1", TermMapMoveStrategy.MOVE)

            assertTrue(result.isSuccess)
            assertEquals(
                "c1",
                repo.observeItem("i0").first()?.collectionId,
            )
            assertEquals(TermMapMoveStrategy.MOVE, repo.lastMoveStrategy)
            assertEquals(listOf("i0"), cache.invalidatedItemIds)
        }

    @Test
    fun `같은 컬렉션으로는 이동할 수 없다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item(collectionId = "c0")) }
            val cache = FakeTranslationCache()

            val result = MoveItemUseCase(repo, cache)("i0", "c0", TermMapMoveStrategy.LEAVE)

            assertEquals(SameCollectionException, result.exceptionOrNull())
            assertTrue(cache.invalidatedItemIds.isEmpty())
        }

    @Test
    fun `없는 아이템 이동은 실패한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val cache = FakeTranslationCache()

            val result = MoveItemUseCase(repo, cache)("ghost", "c1", TermMapMoveStrategy.MERGE)

            assertEquals(ItemNotFoundException, result.exceptionOrNull())
            assertTrue(cache.invalidatedItemIds.isEmpty())
        }
}
