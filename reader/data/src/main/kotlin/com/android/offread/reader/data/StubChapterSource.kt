package com.android.offread.reader.data

/**
 * 챕터 **원문** 스텁. 실제 사이트 수집·본문 파싱은 F-012 인프라 태스크에서 이 자리를 대체한다.
 * 번역 파이프라인(F-020)·캐시(F-021)는 여기서 나온 원문을 실제로 처리한다.
 */
internal object StubChapterSource {
    fun title(chapterIndex: Int): String = "${chapterIndex}화 — 갈림길"

    fun text(chapterIndex: Int): String =
        """
        ルーデウスは分かれ道の前で立ち止まった。左の道は森を抜けてラノア王国へ、右の道は山脈を越えてアスラ王国へ続いている。

        「ソフィアならどちらを選んだろうか。」彼は地図を畳みながらつぶやいた。

        風は谷を抜け、旅人の外套を強くはためかせた。第${chapterIndex}章の終わりが近づいていた。

        風向きが変わった。ルーデウスは左の道へ足を踏み出した。
        """.trimIndent()
}
