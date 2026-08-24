package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.GlossaryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationPromptBuilderTest {
    private val builder = TranslationPromptBuilder()
    private val text = "ルーデウスは分かれ道の前で立ち止まった。"

    @Test
    fun `원문과 방향을 프롬프트에 넣는다`() {
        val prompt = builder.build(text, LanguagePair.JA_KO, emptyList())

        assertTrue(prompt.contains("일본어"))
        assertTrue(prompt.contains("한국어"))
        assertTrue(prompt.contains(text))
    }

    @Test
    fun `용어가 없으면 용어 지시문을 넣지 않는다`() {
        val prompt = builder.build(text, LanguagePair.JA_KO, emptyList())

        assertFalse(prompt.contains("아래 용어는"))
    }

    @Test
    fun `용어맵을 지시문으로 넣고 고정 용어를 앞에 둔다`() {
        val glossary =
            listOf(
                GlossaryEntry("ソフィア", "소피아", pinned = false),
                GlossaryEntry("ルーデウス", "루데우스", pinned = true),
            )

        val prompt = builder.build(text, LanguagePair.JA_KO, glossary)

        assertTrue(prompt.contains("- ルーデウス → 루데우스"))
        assertTrue(prompt.contains("- ソフィア → 소피아"))
        assertTrue(prompt.indexOf("ルーデウス →") < prompt.indexOf("ソフィア →"))
    }

    @Test
    fun `원어나 번역이 빈 용어는 넣지 않는다`() {
        val prompt = builder.build(text, LanguagePair.JA_KO, listOf(GlossaryEntry(" ", "", pinned = true)))

        assertFalse(prompt.contains("아래 용어는"))
    }

    @Test
    fun `응답에서 군더더기를 걷어낸다`() {
        assertEquals("루데우스는 멈춰 섰다.", builder.clean("  번역문: 루데우스는 멈춰 섰다.  "))
        assertEquals("루데우스는 멈춰 섰다.", builder.clean("\"루데우스는 멈춰 섰다.\""))
        assertEquals("루데우스는 멈춰 섰다.", builder.clean("「루데우스는 멈춰 섰다.」"))
    }
}
