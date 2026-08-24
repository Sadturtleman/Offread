package com.android.offread.reader.domain.model

import com.android.offread.core.entity.TranslationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterContentTest {
    private fun content(vararg translated: Boolean) =
        ChapterContent(
            itemId = "i0",
            chapterIndex = 1,
            title = "1화",
            segments =
                translated.mapIndexed { index, done ->
                    ReaderSegment(id = "s$index", original = "原文", translated = if (done) "번역" else null)
                },
        )

    @Test
    fun `세그먼트가 전부 번역되면 오프라인 열람 가능이다`() {
        assertEquals(TranslationStatus.CACHED, content(true, true, true).translationStatus)
    }

    @Test
    fun `일부만 번역되면 번역 중이다`() {
        assertEquals(TranslationStatus.TRANSLATING, content(true, false, false).translationStatus)
    }

    @Test
    fun `하나도 번역되지 않으면 미번역이다`() {
        assertEquals(TranslationStatus.UNTRANSLATED, content(false, false).translationStatus)
    }

    @Test
    fun `빈 챕터는 미번역으로 본다`() {
        assertEquals(TranslationStatus.UNTRANSLATED, content().translationStatus)
    }
}
