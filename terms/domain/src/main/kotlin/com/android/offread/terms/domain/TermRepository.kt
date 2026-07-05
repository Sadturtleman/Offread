package com.android.offread.terms.domain

import com.android.offread.terms.domain.model.Term
import kotlinx.coroutines.flow.Flow

/**
 * 용어맵 저장 포트(헥사고날). 구현체는 Room 어댑터(terms:data). 컬렉션 스코프(P-04 연쇄 삭제).
 */
interface TermRepository {
    fun observeTerms(collectionId: String): Flow<List<Term>>

    /** 추가/수정(id 존재 시 갱신). 생성된/갱신된 id 반환. */
    suspend fun upsert(term: Term): String

    suspend fun delete(id: String)
}
