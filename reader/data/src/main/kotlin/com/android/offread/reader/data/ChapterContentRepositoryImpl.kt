package com.android.offread.reader.data

import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSegment
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [ChapterContentRepository] 스텁 어댑터(F-015). 캔드 세그먼트를 돌려주며, 마지막 세그먼트는 미번역으로 두어
 * 인라인 재시도(P-08)를 시연한다. 실제 온디바이스 번역 파이프라인·세그먼트 캐시(F-020/F-021)는 후속 인프라 태스크.
 */
class ChapterContentRepositoryImpl
    @Inject
    constructor() : ChapterContentRepository {
        override suspend fun getChapter(
            itemId: String,
            chapterIndex: Int,
        ): ChapterContent {
            delay(STUB_LATENCY_MILLIS)
            return ChapterContent(
                itemId = itemId,
                chapterIndex = chapterIndex,
                title = "${chapterIndex}화 — 갈림길",
                segments =
                    listOf(
                        ReaderSegment(
                            id = "seg-1",
                            original = "ルーデウスは分かれ道の前で立ち止まった。",
                            translated = "루데우스는 갈림길 앞에 멈춰 섰다. 왼쪽 길은 숲을 지나 라노아 왕국으로, 오른쪽 길은 산맥을 넘어 아스라 왕국으로 이어진다.",
                        ),
                        ReaderSegment(
                            id = "seg-2",
                            original = "「ソフィアならどちらを選んだろうか。」",
                            translated = "\"소피아라면 어느 쪽을 골랐을까.\" 그는 지도를 접으며 중얼거렸다.",
                        ),
                        // 미번역 세그먼트 — 화면에서 원문 + 재시도로 노출(P-08)
                        ReaderSegment(
                            id = "seg-3",
                            original = "風は谷を抜け、旅人の外套を強くはためかせた。",
                            translated = null,
                        ),
                        ReaderSegment(
                            id = "seg-4",
                            original = "風向きが変わった。",
                            translated = "바람이 방향을 바꿨다. 루데우스는 왼쪽 길로 걸음을 옮겼다.",
                        ),
                    ),
            )
        }

        override suspend fun retrySegment(
            itemId: String,
            chapterIndex: Int,
            segmentId: String,
        ): String {
            delay(STUB_LATENCY_MILLIS)
            return "바람이 골짜기를 빠져나가 나그네의 외투를 세차게 나부끼게 했다."
        }

        private companion object {
            const val STUB_LATENCY_MILLIS = 300L
        }
    }
