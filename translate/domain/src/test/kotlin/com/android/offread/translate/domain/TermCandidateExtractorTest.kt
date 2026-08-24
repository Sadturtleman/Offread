package com.android.offread.translate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermCandidateExtractorTest {
    private val extractor = TermCandidateExtractor()

    @Test
    fun `반복되는 카타카나 고유명사를 후보로 뽑는다`() {
        val originals =
            listOf(
                "ルーデウスは分かれ道の前で立ち止まった。",
                "ルーデウスは地図を畳んだ。",
            )

        val candidates = extractor.extract(originals)

        assertEquals(listOf("ルーデウス"), candidates.map { it.source })
        assertEquals(listOf(2), candidates.map { it.occurrenceCount })
    }

    @Test
    fun `한 번만 나온 표기는 제안하지 않는다`() {
        val candidates = extractor.extract(listOf("ソフィアは本を閉じた。"))

        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `이미 용어맵에 있는 원어는 다시 제안하지 않는다`() {
        val originals = List(3) { "ルーデウスとソフィアが歩く。" }

        val candidates = extractor.extract(originals, known = setOf("ルーデウス"))

        assertEquals(listOf("ソフィア"), candidates.map { it.source })
    }

    @Test
    fun `출현 수가 많은 후보를 먼저 제안하고 개수를 제한한다`() {
        val originals =
            listOf(
                "アアアが来た。".repeat(4),
                "イイイが来た。".repeat(3),
                "ウウウが来た。".repeat(2),
            )

        val candidates = extractor.extract(originals, minOccurrences = 2, limit = 2)

        assertEquals(listOf("アアア", "イイイ"), candidates.map { it.source })
    }

    @Test
    fun `장음과 중점만 남는 조각은 후보가 아니다`() {
        val candidates = extractor.extract(listOf("ーー ・・ ーー ・・"))

        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `카타카나가 없는 원문에서는 후보가 나오지 않는다`() {
        val candidates = extractor.extract(listOf("彼は道の前で立ち止まった。", "彼は地図を畳んだ。"))

        assertTrue(candidates.isEmpty())
    }
}

class TermCandidateExtractorWordsTest {
    private val extractor = TermCandidateExtractor()

    @Test
    fun `한 번만 나온 표기도 롱프레스 후보로는 돌려준다`() {
        val words = extractor.wordsIn("ルーデウスは地図を畳んだ。")

        assertTrue(words.contains("ルーデウス"))
    }

    @Test
    fun `카타카나와 한자 덩어리를 모두 고를 수 있다`() {
        val words = extractor.wordsIn("聖剣アクアを抜いた。")

        assertEquals(listOf("聖剣", "アクア"), words)
    }

    @Test
    fun `같은 표기는 한 번만 노출한다`() {
        val words = extractor.wordsIn("ソフィアとソフィア。")

        assertEquals(listOf("ソフィア"), words)
    }

    @Test
    fun `한 글자 표기는 후보가 아니다`() {
        assertTrue(extractor.wordsIn("彼は。").isEmpty())
    }
}
