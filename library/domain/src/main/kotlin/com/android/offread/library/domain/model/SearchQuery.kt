package com.android.offread.library.domain.model

import com.android.offread.core.entity.ItemType

/**
 * F-010 검색 조건. 서버 없이 로컬 Room 에서만 조회한다(P-01).
 *
 * @property text 제목·작가 부분 일치. 공백만 있으면 검색하지 않는다.
 * @property collectionId 지정 시 해당 컬렉션 내 검색, null 이면 전역 검색
 * @property type 지정 시 유형 필터, null 이면 전체
 */
data class SearchQuery(
    val text: String = "",
    val collectionId: String? = null,
    val type: ItemType? = null,
) {
    val isBlank: Boolean get() = text.isBlank()

    /**
     * 인메모리 매칭 규칙. Room 어댑터의 `ItemDao.search` 와 같은 의미를 갖는 단일 정의로,
     * 테스트 더블이 SQL 과 어긋나지 않게 여기를 기준으로 삼는다.
     */
    fun matches(item: LibraryItem): Boolean {
        val needle = text.trim()
        if (needle.isEmpty()) return false
        val hit =
            item.title.contains(needle, ignoreCase = true) ||
                item.author.contains(needle, ignoreCase = true)
        return hit &&
            (collectionId == null || item.collectionId == collectionId) &&
            (type == null || item.type == type)
    }
}
