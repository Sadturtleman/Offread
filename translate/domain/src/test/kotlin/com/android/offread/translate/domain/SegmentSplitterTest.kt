package com.android.offread.translate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SegmentSplitterTest {
    private val splitter = SegmentSplitter()

    @Test
    fun `빈 줄을 기준으로 문단을 나눈다`() {
        val segments = splitter.split("첫 문단.\n\n둘째 문단.")

        assertEquals(listOf("첫 문단.", "둘째 문단."), segments.map { it.original })
        assertEquals(listOf("seg-1", "seg-2"), segments.map { it.id })
    }

    @Test
    fun `문단 안의 줄바꿈은 유지한다`() {
        val segments = splitter.split("대사 한 줄\n다음 줄")

        assertEquals(1, segments.size)
        assertEquals("대사 한 줄\n다음 줄", segments.first().original)
    }

    @Test
    fun `빈 문단과 앞뒤 공백은 버린다`() {
        val segments = splitter.split("\n\n  본문.  \n\n\n\n")

        assertEquals(listOf("본문."), segments.map { it.original })
    }

    @Test
    fun `긴 문단은 문장 경계에서 자르고 부호를 앞 문장에 남긴다`() {
        val sentence = "ルーデウスは分かれ道の前で立ち止まった。"
        val paragraph = sentence.repeat(10)

        val segments = splitter.split(paragraph, maxChars = 60)

        assertTrue(segments.size > 1)
        assertTrue(segments.all { it.original.length <= 60 })
        assertTrue(segments.all { it.original.endsWith("。") })
        assertEquals(paragraph, segments.joinToString("") { it.original })
    }

    @Test
    fun `문장부호가 없는 긴 문단도 통째로 한 세그먼트가 된다`() {
        val paragraph = "가".repeat(120)

        val segments = splitter.split(paragraph, maxChars = 50)

        assertEquals(listOf(paragraph), segments.map { it.original })
    }

    @Test
    fun `빈 입력은 세그먼트가 없다`() {
        assertTrue(splitter.split("   \n\n  ").isEmpty())
    }
}
