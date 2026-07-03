package com.android.offread.library.domain.model

/**
 * 라이브러리 컬렉션(폴더). 무제한 중첩([parentId]).
 *
 * @property itemCount 하위 아이템(작품/문서) 수 — 표시용
 * @property termCount 컬렉션 스코프 용어 수 — 표시용
 * @property updatedAt 최근 갱신(정렬용, epoch millis)
 */
data class Collection(
    val id: String,
    val name: String,
    val parentId: String?,
    val itemCount: Int,
    val termCount: Int,
    val updatedAt: Long,
)
