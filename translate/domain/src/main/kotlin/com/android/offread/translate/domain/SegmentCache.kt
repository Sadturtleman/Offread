package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.SegmentCacheKey

/**
 * F-021 세그먼트 캐시 포트(헥사고날). 히트 시 추론 없이 즉시 표시하고,
 * 캐시된 항목은 완전 오프라인 열람을 보장한다(§5).
 */
interface SegmentCache {
    /** 캐시된 번역문(없으면 null). */
    suspend fun get(key: SegmentCacheKey): String?

    /** 번역 결과를 저장한다. [itemId]·[chapterIndex] 는 무효화·용량 집계용 메타다. */
    suspend fun put(
        key: SegmentCacheKey,
        itemId: String,
        chapterIndex: Int,
        translation: String,
    )

    /** 아이템 캐시 무효화(F-007 컬렉션 이동, F-017 용어 편집 후 재번역). */
    suspend fun invalidateItem(itemId: String)

    /** 챕터 단위 무효화(F-017 현재 챕터 재번역). */
    suspend fun invalidateChapter(
        itemId: String,
        chapterIndex: Int,
    )

    /** 컬렉션 캐시 무효화(용어맵 변경 등 컬렉션 전체에 영향이 갈 때). */
    suspend fun invalidateCollection(collectionId: String)

    /** 캐시 사용량(F-031 저장·캐시 관리). */
    suspend fun stats(): CacheStats

    /** 아이템별 캐시 사용량(F-031 콘텐츠 목록). 캐시가 없는 아이템은 빠진다. */
    suspend fun usageByItem(): Map<String, CacheStats>

    /** 전체 비우기(F-031, 정책 P-04). */
    suspend fun clear()
}

/**
 * 캐시 사용량 요약.
 *
 * @property entryCount 저장된 세그먼트 수
 * @property bytes 번역문 바이트 합계(UTF-8 기준 근사)
 */
data class CacheStats(
    val entryCount: Int,
    val bytes: Long,
) {
    companion object {
        val EMPTY = CacheStats(entryCount = 0, bytes = 0)
    }
}
