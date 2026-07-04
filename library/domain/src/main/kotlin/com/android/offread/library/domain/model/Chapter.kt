package com.android.offread.library.domain.model

import com.android.offread.core.entity.TranslationStatus

/**
 * 웹소설 챕터 1개(L-03).
 *
 * NOTE: 실제 챕터 목록·제목·화별 읽음 상태는 스크래핑(F-012 확장)·리더(F-015)에서 채워진다.
 * 현재는 아이템의 화수로부터 합성한다.
 *
 * @property index 1-based 화 번호
 */
data class Chapter(
    val index: Int,
    val title: String,
    val translationStatus: TranslationStatus,
)
