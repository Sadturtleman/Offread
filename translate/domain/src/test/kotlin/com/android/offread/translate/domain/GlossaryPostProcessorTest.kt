package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.GlossaryEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class GlossaryPostProcessorTest {
    private val postProcessor = GlossaryPostProcessor()

    @Test
    fun `고정 용어가 원어로 남아 있으면 캐논값으로 치환한다`() {
        val glossary = listOf(GlossaryEntry("ルーデウス", "루데우스", pinned = true))

        val result = postProcessor.apply("ルーデウス는 길 앞에 멈춰 섰다.", glossary)

        assertEquals("루데우스는 길 앞에 멈춰 섰다.", result)
    }

    @Test
    fun `고정이 아닌 용어는 모델 판단을 존중해 건드리지 않는다`() {
        val glossary = listOf(GlossaryEntry("ソフィア", "소피아", pinned = false))

        val result = postProcessor.apply("ソフィア가 책을 덮었다.", glossary)

        assertEquals("ソフィア가 책을 덮었다.", result)
    }

    @Test
    fun `긴 용어를 먼저 치환해 짧은 용어가 잠식하지 않는다`() {
        val glossary =
            listOf(
                GlossaryEntry("聖剣", "성검", pinned = true),
                GlossaryEntry("聖剣アクア", "아쿠아 성검", pinned = true),
            )

        val result = postProcessor.apply("彼は聖剣アクアを抜いた。", glossary)

        assertEquals("彼は아쿠아 성검を抜いた。", result)
    }

    @Test
    fun `용어가 없으면 번역문을 그대로 둔다`() {
        assertEquals("그대로.", postProcessor.apply("그대로.", emptyList()))
    }
}
