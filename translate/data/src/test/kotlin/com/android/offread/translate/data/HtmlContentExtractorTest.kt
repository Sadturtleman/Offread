package com.android.offread.translate.data

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlContentExtractorTest {
    private val url = "https://example.com/novel/1"

    private fun extract(html: String) = HtmlContentExtractor.extract(url, Jsoup.parse(html))

    /** Readability 는 본문이 어느 정도 길어야 본문으로 인정한다. */
    private fun longBody(paragraph: String) = paragraph.repeat(8)

    @Test
    fun `내비게이션과 사이드바를 걷어내고 본문만 남긴다`() {
        val body = longBody("ルーデウスは分かれ道の前で立ち止まった。")
        val html =
            """
            <html><body>
              <nav><p>ホーム | ランキング | ログイン</p></nav>
              <aside><p>広告です。今すぐ登録。</p></aside>
              <article><p>$body</p><p>$body</p></article>
              <footer><p>利用規約 プライバシー</p></footer>
            </body></html>
            """.trimIndent()

        val text = extract(html)

        assertTrue(text.contains("ルーデウス"))
        assertFalse(text.contains("ランキング"))
        assertFalse(text.contains("広告"))
        assertFalse(text.contains("利用規約"))
    }

    @Test
    fun `일본어 루비 읽기는 본문에서 빼고 한자만 남긴다`() {
        val html =
            """
            <html><body><article>
              <p>${"<ruby>魔力<rp>(</rp><rt>まりょく</rt><rp>)</rp></ruby>を高めた。".repeat(8)}</p>
            </article></body></html>
            """.trimIndent()

        val text = extract(html)

        assertTrue(text.contains("魔力"))
        assertFalse(text.contains("まりょく"))
    }

    @Test
    fun `문단 경계를 살린다`() {
        val first = longBody("最初の段落です。")
        val second = longBody("次の段落です。")
        val html = "<html><body><article><p>$first</p><p>$second</p></article></body></html>"

        val text = extract(html)

        assertEquals(listOf(first, second), text.split("\n\n"))
    }

    @Test
    fun `스크립트와 스타일은 번역 대상에서 뺀다`() {
        val body = longBody("本文です。")
        val html =
            """
            <html><body><article>
              <script>var a = "スクリプトの中身";</script>
              <style>.x { color: red; }</style>
              <p>$body</p>
            </article></body></html>
            """.trimIndent()

        val text = extract(html)

        assertTrue(text.contains("本文です。"))
        assertFalse(text.contains("スクリプトの中身"))
        assertFalse(text.contains("color: red"))
    }

    @Test
    fun `본문을 못 찾으면 body 로 물러난다`() {
        val html = "<html><body>短い本文です。</body></html>"

        assertTrue(extract(html).contains("短い本文です。"))
    }

    @Test
    fun `내용이 없으면 빈 문자열이다`() {
        assertEquals("", extract("<html><body></body></html>"))
    }
}
