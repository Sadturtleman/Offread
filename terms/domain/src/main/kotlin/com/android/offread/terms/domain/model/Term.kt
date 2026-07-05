package com.android.offread.terms.domain.model

/** 용어 출처. */
enum class TermOrigin {
    /** 번역 중 자동 추출(F-024). */
    AUTO,

    /** 사용자 수동 추가. */
    MANUAL,
}

/** 용어 상태. */
enum class TermStatus {
    /** 확정 — 번역에 적용된다. */
    CONFIRMED,

    /** 자동 제안 — 사용자 수락 전(F-024). */
    SUGGESTED,
}

/** 용어맵 목록 필터(T-01). */
enum class TermFilter {
    ALL,
    AUTO,
    MANUAL,
    PINNED,
}

/**
 * 컬렉션 스코프 용어 1개(F-024~F-026).
 *
 * @property source 원어
 * @property translation 캐논 번역값
 * @property pinned 고정 — 번역 시 강제 적용(후처리 치환)
 * @property occurrenceCount 출현 수(자동 추출 기준)
 */
data class Term(
    val id: String,
    val collectionId: String,
    val source: String,
    val translation: String,
    val pinned: Boolean,
    val origin: TermOrigin,
    val occurrenceCount: Int,
    val status: TermStatus,
    val updatedAt: Long,
)
