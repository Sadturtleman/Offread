package com.android.offread.library.domain

/**
 * 번역 캐시 무효화 포트(헥사고날). 캐시 키에 collectionId 가 포함되므로(F-021)
 * 아이템이 다른 컬렉션으로 이동하면 해당 아이템 캐시를 무효화해야 한다(F-007).
 */
interface TranslationCache {
    suspend fun invalidateItem(itemId: String)
}
