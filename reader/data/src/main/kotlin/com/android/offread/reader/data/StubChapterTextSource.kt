package com.android.offread.reader.data

import com.android.offread.translate.domain.ChapterTextSource
import javax.inject.Inject

/**
 * 챕터 **원문** 스텁 어댑터. 실제 사이트 수집·본문 파싱은 F-012 인프라 태스크에서 이 클래스를
 * 대체한다. 리더와 선번역 큐(F-022)가 같은 원문을 쓰도록 포트 하나로 모았다.
 */
class StubChapterTextSource
    @Inject
    constructor() : ChapterTextSource {
        override fun title(chapterIndex: Int): String = "${chapterIndex}화 — 갈림길"

        override suspend fun text(
            itemId: String,
            chapterIndex: Int,
        ): String =
            """
            ルーデウスは分かれ道の前で立ち止まった。左の道は森を抜けてラノア王国へ、右の道は山脈を越えてアスラ王国へ続いている。

            「ソフィアならどちらを選んだろうか。」彼は地図を畳みながらつぶやいた。

            風は谷を抜け、旅人の外套を強くはためかせた。第${chapterIndex}章の終わりが近づいていた。

            風向きが変わった。ルーデウスは左の道へ足を踏み出した。
            """.trimIndent()
    }
