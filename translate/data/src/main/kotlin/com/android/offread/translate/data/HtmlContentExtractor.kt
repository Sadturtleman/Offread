package com.android.offread.translate.data

import net.dankito.readability4j.extended.Readability4JExtended
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * HTML 에서 번역할 본문 텍스트를 뽑는다.
 *
 * 본문 판별은 Mozilla Readability 의 코틀린 포트(Readability4J)에 맡긴다 — 광고·내비게이션·
 * 사이드바 제거가 손으로 짠 셀렉터보다 훨씬 많은 사이트에서 검증돼 있고, 결과가 Firefox
 * 리더뷰와 같다. Readability 가 본문을 못 찾으면(짧은 페이지 등) body 전체로 물러난다 —
 * 본문만 정확히 집는 것보다 **번역할 거리를 얻는 것**이 우선이다.
 */
internal object HtmlContentExtractor {
    fun extract(
        url: String,
        document: Document,
    ): String {
        document.select("script, style, noscript").remove()
        // 일본어 특화: 루비(후리가나)를 지운다. 남겨 두면 "漢字かんじ" 처럼 읽기가 본문에 섞여
        // 번역 품질이 떨어진다. <rp> 는 루비 미지원 브라우저용 괄호라 함께 지운다.
        document.select("rt, rp").remove()

        val article =
            runCatching { Readability4JExtended(url, document.outerHtml()).parse() }.getOrNull()
        val extracted = article?.articleContent?.let { textOf(it) }.orEmpty()
        val text = extracted.ifBlank { document.body()?.let { textOf(it) }.orEmpty() }
        return text.normalize()
    }

    /** 문단 태그가 있으면 문단 경계를 살린다 — 세그먼트 분할 품질이 올라간다. */
    private fun textOf(element: Element): String {
        val paragraphs = element.select("p").filter { it.text().isNotBlank() }
        return if (paragraphs.isEmpty()) {
            element.wholeText()
        } else {
            paragraphs.joinToString("\n\n") { it.text() }
        }
    }

    private fun String.normalize(): String =
        lines()
            .joinToString("\n") { it.trim() }
            .replace(BLANK_RUN, "\n\n")
            .trim()

    private val BLANK_RUN = Regex("\\n{3,}")
}
