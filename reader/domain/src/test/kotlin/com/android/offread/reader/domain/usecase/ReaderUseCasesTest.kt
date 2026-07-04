package com.android.offread.reader.domain.usecase

import com.android.offread.reader.domain.FakeChapterContentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReaderUseCasesTest {
    private val contentRepo = FakeChapterContentRepository()

    @Test
    fun `챕터 본문을 가져오고 미번역 세그먼트는 원문을 노출한다`() =
        runTest {
            val content = GetChapterContentUseCase(contentRepo)("i0", 124)

            assertEquals("124화", content.title)
            val untranslated = content.segments.first { !it.isTranslated }
            assertFalse(untranslated.isTranslated)
            assertEquals("原文2", untranslated.displayText)
        }

    @Test
    fun `0 이하 화 번호는 1화로 클램프된다`() =
        runTest {
            val content = GetChapterContentUseCase(contentRepo)("i0", 0)

            assertEquals(1, content.chapterIndex)
        }

    @Test
    fun `세그먼트 재시도는 번역문을 반환한다`() =
        runTest {
            val translated = RetrySegmentUseCase(contentRepo)("i0", 124, "s2")

            assertEquals("재번역-s2", translated)
        }
}
