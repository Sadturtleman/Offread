package com.android.offread.translate.data

import org.jsoup.nodes.Document

/**
 * HTML 에서 번역할 본문 텍스트를 뽑는다.
 *
 * 사이트마다 구조가 달라 완벽할 수 없다. 흔한 본문 컨테이너를 먼저 찾고, 못 찾으면 body 전체로
 * 물러난다 — 본문만 정확히 집는 것보다 **번역할 거리를 얻는 것**이 MVP 에서는 중요하다.
 * 스크립트·스타일은 항상 걷어낸다.
 */
internal object HtmlContentExtractor {
    fun extract(document: Document): String {
        document.select("script, style, noscript").remove()
        val container = CONTENT_SELECTORS.firstNotNullOfOrNull { document.selectFirst(it) } ?: document.body()
        val paragraphs = container?.select("p")
        val raw =
            if (paragraphs.isNullOrEmpty()) {
                container?.wholeText().orEmpty()
            } else {
                // 문단 태그가 있으면 문단 경계를 살린다 — 세그먼트 분할 품질이 올라간다.
                paragraphs.joinToString("\n\n") { it.text() }
            }
        return raw
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(BLANK_RUN, "\n\n")
            .trim()
    }

    /** 흔한 본문 컨테이너 후보. 앞에서부터 먼저 걸리는 것을 쓴다. */
    private val CONTENT_SELECTORS =
        listOf(
            "#novel_honbun",
            ".novel_view",
            ".widget-episodeBody",
            "article",
            "main",
            "#content",
            ".content",
        )
    private val BLANK_RUN = Regex("\\n{3,}")
}
