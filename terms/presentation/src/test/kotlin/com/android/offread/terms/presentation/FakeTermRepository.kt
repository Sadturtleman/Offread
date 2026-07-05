package com.android.offread.terms.presentation

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 인메모리 [TermRepository] 더블(presentation 단위 테스트용). */
class FakeTermRepository : TermRepository {
    private val terms = MutableStateFlow<List<Term>>(emptyList())
    private var nextId = 0

    override fun observeTerms(collectionId: String): Flow<List<Term>> =
        terms.map { list -> list.filter { it.collectionId == collectionId } }

    override suspend fun upsert(term: Term): String {
        val id = term.id.ifBlank { "t${nextId++}" }
        terms.value = terms.value.filterNot { it.id == id } + term.copy(id = id)
        return id
    }

    override suspend fun delete(id: String) {
        terms.value = terms.value.filterNot { it.id == id }
    }

    fun current(): List<Term> = terms.value

    fun seed(term: Term) {
        terms.value = terms.value + term
    }
}
