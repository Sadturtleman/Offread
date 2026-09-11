package com.android.offread.translate.domain.model

/** 번역 단위 원문 조각(F-020 세그먼트 분할 결과). */
data class Segment(
    val id: String,
    val original: String,
)

/**
 * 웹뷰가 페이지에서 긁어 온 텍스트 노드 하나.
 *
 * @property id 페이지 안에서의 노드 식별자. 번역문을 되돌려 줄 때 이 값으로 자리를 찾는다.
 */
data class VisibleText(
    val id: String,
    val text: String,
)

/**
 * 노드 하나의 번역 결과.
 *
 * @property translated null 이면 추론 실패 — 그 자리는 원문 그대로 둔다.
 */
data class TranslatedText(
    val id: String,
    val translated: String?,
)
