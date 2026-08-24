package com.android.offread.translate.domain

/**
 * 자동 제안 용어 저장 포트(F-024). 어댑터가 용어맵(terms)에 SUGGESTED·AUTO 로 적는다.
 * 제안은 사용자가 수락하기 전까지 번역에 쓰이지 않는다.
 */
interface TermSuggestionSink {
    /** 이미 용어맵에 있는 원어들(확정·제안 모두). 같은 용어를 반복 제안하지 않기 위함. */
    suspend fun existingSources(collectionId: String): Set<String>

    suspend fun suggest(
        collectionId: String,
        source: String,
        translation: String,
        occurrenceCount: Int,
    )
}
