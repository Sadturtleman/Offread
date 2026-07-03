package com.android.offread.importer.domain.usecase

import com.android.offread.importer.domain.FakeWebNovelImporter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognizeWebNovelUseCaseTest {
    private val useCase = RecognizeWebNovelUseCase(FakeWebNovelImporter())

    @Test
    fun `지원 사이트 URL 을 인식한다`() =
        runTest {
            val result = useCase("https://ncode.syosetu.com/n9669bk/")

            assertTrue(result.isSuccess)
            assertEquals("무직전생", result.getOrThrow().title)
            assertEquals(286, result.getOrThrow().totalChapters)
        }

    @Test
    fun `빈 URL 은 실패한다`() =
        runTest {
            val result = useCase("   ")

            assertTrue(result.exceptionOrNull() is EmptyUrlException)
        }

    @Test
    fun `미지원 사이트는 실패한다`() =
        runTest {
            val result = useCase("https://example.com/story/1")

            assertTrue(result.exceptionOrNull() is UnsupportedSiteException)
        }
}
