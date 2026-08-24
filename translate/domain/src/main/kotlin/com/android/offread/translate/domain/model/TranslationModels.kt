package com.android.offread.translate.domain.model

import com.android.offread.core.entity.LanguagePair

/** 번역 단위 원문 조각(F-020 세그먼트 분할 결과). */
data class Segment(
    val id: String,
    val original: String,
)

/**
 * 번역 결과 세그먼트.
 *
 * @property translated 번역문. null 이면 추론 실패 — 화면은 원문을 노출하고 재시도를 제공한다(P-08).
 * @property fromCache 캐시 히트로 추론 없이 나온 결과인지(F-021)
 */
data class TranslatedSegment(
    val id: String,
    val original: String,
    val translated: String?,
    val fromCache: Boolean = false,
)

/**
 * 번역 시 강제·참고되는 용어 1개. terms 의 Term 을 번역 코어가 쓰는 최소 형태로 좁힌 것으로,
 * 두 feature 도메인이 서로를 모르게 한다.
 *
 * @property pinned 고정 — 후처리에서 강제 치환한다(F-026)
 */
data class GlossaryEntry(
    val source: String,
    val translation: String,
    val pinned: Boolean,
)

/**
 * 한 챕터 번역 요청(F-020).
 *
 * @property collectionId 용어맵·캐시 스코프(§5)
 */
data class TranslationRequest(
    val itemId: String,
    val collectionId: String,
    val chapterIndex: Int,
    val languagePair: LanguagePair,
    val segments: List<Segment>,
)
