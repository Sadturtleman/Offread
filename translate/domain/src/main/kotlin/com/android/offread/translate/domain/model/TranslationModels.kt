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
 * @property translated 번역문. null 이면 추론 실패 — 화면은 원문을 노출하고 재시도를 제공한다.
 * @property fromCache 캐시 히트로 추론 없이 나온 결과인지
 */
data class TranslatedSegment(
    val id: String,
    val original: String,
    val translated: String?,
    val fromCache: Boolean = false,
)

/** 수집한 웹페이지 원문. */
data class WebPage(
    val url: String,
    val title: String,
    val text: String,
)

/** 번역해서 보여줄 페이지 하나. */
data class TranslatedPage(
    val url: String,
    val title: String,
    val languagePair: LanguagePair,
    val segments: List<TranslatedSegment>,
)
