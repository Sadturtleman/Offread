package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.SegmentCacheKey

/**
 * 세그먼트 번역 캐시 포트. 히트하면 추론 없이 즉시 표시한다 —
 * 온디바이스 4B 추론은 문단당 수 초가 걸릴 수 있어 재방문 체감을 좌우한다.
 */
interface SegmentCache {
    suspend fun get(key: SegmentCacheKey): String?

    suspend fun put(
        key: SegmentCacheKey,
        translation: String,
    )

    /** 캐시 사용량(문단 수, 바이트). */
    suspend fun stats(): CacheStats

    suspend fun clear()
}

data class CacheStats(
    val entryCount: Int,
    val bytes: Long,
) {
    companion object {
        val EMPTY = CacheStats(entryCount = 0, bytes = 0)
    }
}
