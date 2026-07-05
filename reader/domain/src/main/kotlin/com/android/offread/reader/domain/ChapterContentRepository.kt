package com.android.offread.reader.domain

import com.android.offread.reader.domain.model.ChapterContent

/**
 * 챕터 본문 제공 포트(헥사고날). 온디바이스 번역 파이프라인(F-020)·세그먼트 캐시(F-021)는
 * 이 포트 뒤 어댑터(reader:data)에 감춘다.
 */
interface ChapterContentRepository {
    /** 특정 아이템의 특정 화 본문을 가져온다. */
    suspend fun getChapter(
        itemId: String,
        chapterIndex: Int,
    ): ChapterContent

    /** 미번역/실패 세그먼트를 재번역한다. 성공 시 번역문을 반환한다(P-08 인라인 재시도). */
    suspend fun retrySegment(
        itemId: String,
        chapterIndex: Int,
        segmentId: String,
    ): String
}
