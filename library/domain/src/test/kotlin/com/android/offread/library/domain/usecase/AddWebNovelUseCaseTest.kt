package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.FakeLibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddWebNovelUseCaseTest {
    private fun request(collectionId: String = "c0") =
        AddWebNovelUseCase.Request(
            collectionId = collectionId,
            title = "무직전생",
            author = "理不尽な孫の手",
            sourceUrl = "https://ncode.syosetu.com/n9669bk/",
            siteName = "소설가가 되자",
            totalChapters = 286,
            serialStatus = SerialStatus.ONGOING,
        )

    @Test
    fun `웹소설을 미번역 상태로 컬렉션에 저장한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val result = AddWebNovelUseCase(repo)(request())

            assertTrue(result.isSuccess)
            val item = repo.currentItems().single()
            assertEquals("무직전생", item.title)
            assertEquals(ItemType.WEBNOVEL, item.type)
            assertEquals(TranslationStatus.UNTRANSLATED, item.translationStatus)
            assertEquals("c0", item.collectionId)
        }

    @Test
    fun `컬렉션을 지정하지 않으면 실패한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val result = AddWebNovelUseCase(repo)(request(collectionId = ""))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NoTargetCollectionException)
            assertTrue(repo.currentItems().isEmpty())
        }
}
