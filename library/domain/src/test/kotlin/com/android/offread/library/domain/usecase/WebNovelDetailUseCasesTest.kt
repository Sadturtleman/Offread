package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WebNovelDetailUseCasesTest {
    private fun item(
        id: String = "i0",
        chapters: Int = 3,
        status: TranslationStatus = TranslationStatus.UNTRANSLATED,
    ) = LibraryItem(
        id = id,
        collectionId = "c0",
        type = ItemType.WEBNOVEL,
        title = "무직전생",
        author = "손의 손",
        sourceUrl = "https://ncode.syosetu.com/n9669bk/",
        siteName = "소설가가 되자",
        totalChapters = chapters,
        serialStatus = SerialStatus.ONGOING,
        translationStatus = status,
        updatedAt = 0,
    )

    @Test
    fun `화수만큼 챕터를 합성한다`() {
        val chapters = GetChaptersUseCase()(item(chapters = 3))

        assertEquals(3, chapters.size)
        assertEquals(1, chapters.first().index)
        assertEquals("3화", chapters.last().title)
    }

    @Test
    fun `아이템을 id 로 구독한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedItem(item(id = "i7"))

            assertEquals("무직전생", GetItemUseCase(repo)("i7").first()?.title)
        }

    @Test
    fun `오프라인 준비는 번역 상태를 캐시됨으로 전이한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedItem(item(id = "i9", status = TranslationStatus.UNTRANSLATED))

            PrepareOfflineUseCase(repo)("i9")

            assertEquals(TranslationStatus.CACHED, GetItemUseCase(repo)("i9").first()?.translationStatus)
        }
}
