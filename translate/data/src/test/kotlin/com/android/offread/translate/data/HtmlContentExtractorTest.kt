package com.android.offread.translate.data

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlContentExtractorTest {
    private fun extract(html: String) = HtmlContentExtractor.extract(Jsoup.parse(html))

    @Test
    fun `본문 컨테이너를 찾아 문단만 뽑는다`() {
        val html =
            """
            <html><body>
              <nav><p>메뉴</p></nav>
              <div id="novel_honbun"><p>ルーデウスは歩いた。</p><p>風が吹いた。</p></div>
            </body></html>
            """.trimIndent()

        assertEquals("ルーデウスは歩いた。\n\n風が吹いた。", extract(html))
    }

    @Test
    fun `컨테이너를 못 찾으면 body 전체로 물러난다`() {
        val html = "<html><body><p>本文です。</p></body></html>"

        assertEquals("本文です。", extract(html))
    }

    @Test
    fun `스크립트와 스타일은 번역 대상에서 뺀다`() {
        val html =
            """
            <html><body><article>
              <script>var a = "スクリプト";</script>
              <style>.x { color: red; }</style>
              <p>本文です。</p>
            </article></body></html>
            """.trimIndent()

        val text = extract(html)

        assertEquals("本文です。", text)
        assertFalse(text.contains("スクリプト"))
    }

    @Test
    fun `문단 태그가 없으면 텍스트를 통째로 쓴다`() {
        val html = "<html><body><article>本文です。<br>次の行。</article></body></html>"

        val text = extract(html)

        assertTrue(text.contains("本文です。"))
        assertTrue(text.contains("次の行。"))
    }

    @Test
    fun `빈 줄이 이어지면 하나로 줄인다`() {
        val html = "<html><body><article>첫 줄.\n\n\n\n둘째 줄.</article></body></html>"

        assertEquals("첫 줄.\n\n둘째 줄.", extract(html))
    }

    @Test
    fun `본문이 없으면 빈 문자열이다`() {
        assertEquals("", extract("<html><body></body></html>"))
    }
}
