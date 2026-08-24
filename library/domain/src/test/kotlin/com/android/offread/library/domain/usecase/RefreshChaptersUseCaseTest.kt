package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.ChapterSource
import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeChapterSource(
    private val result: Result<Int>,
) : ChapterSource {
    var calledWith: String? = null

    override suspend fun fetchTotalChapters(sourceUrl: String): Int {
        calledWith = sourceUrl
        return result.getOrThrow()
    }
}

class RefreshChaptersUseCaseTest {
    private val repo = FakeLibraryRepository()

    private fun seedItem(totalChapters: Int = 10) {
        repo.seedItem(
            LibraryItem(
                id = "i0",
                collectionId = "c0",
                type = ItemType.WEBNOVEL,
                title = "무직전생",
                author = "손의 손",
                sourceUrl = "https://ncode.syosetu.com/n9669bk/",
                siteName = "소설가가 되자",
                totalChapters = totalChapters,
                serialStatus = SerialStatus.ONGOING,
                translationStatus = TranslationStatus.UNTRANSLATED,
                updatedAt = 0,
            ),
        )
    }

    @Test
    fun `새 화가 있으면 화수를 갱신하고 늘어난 수를 돌려준다`() =
        runTest {
            seedItem(totalChapters = 10)
            val source = FakeChapterSource(Result.success(13))

            val added = RefreshChaptersUseCase(repo, source)("i0")

            assertEquals(3, added.getOrNull())
            assertEquals(13, repo.observeItem("i0").first()?.totalChapters)
            assertEquals("https://ncode.syosetu.com/n9669bk/", source.calledWith)
        }

    @Test
    fun `새 화가 없으면 0 이고 화수를 건드리지 않는다`() =
        runTest {
            seedItem(totalChapters = 10)

            val added = RefreshChaptersUseCase(repo, FakeChapterSource(Result.success(10)))("i0")

            assertEquals(0, added.getOrNull())
            assertEquals(10, repo.observeItem("i0").first()?.totalChapters)
        }

    @Test
    fun `사이트에서 화가 줄어 보여도 목록을 깎지 않는다`() =
        runTest {
            seedItem(totalChapters = 10)

            val added = RefreshChaptersUseCase(repo, FakeChapterSource(Result.success(7)))("i0")

            assertEquals(0, added.getOrNull())
            assertEquals(10, repo.observeItem("i0").first()?.totalChapters)
        }

    @Test
    fun `수집 실패는 실패로 전달한다`() =
        runTest {
            seedItem()
            val source = FakeChapterSource(Result.failure(IllegalArgumentException("지원하지 않는 사이트예요.")))

            val result = RefreshChaptersUseCase(repo, source)("i0")

            assertTrue(result.isFailure)
            assertEquals("지원하지 않는 사이트예요.", result.exceptionOrNull()?.message)
        }

    @Test
    fun `없는 아이템은 실패다`() =
        runTest {
            val result = RefreshChaptersUseCase(repo, FakeChapterSource(Result.success(1)))("missing")

            assertEquals(ItemNotFoundException, result.exceptionOrNull())
        }
}
