package com.android.offread.terms.data

import com.android.offread.core.database.TermDao
import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/** [TermRepository] 의 Room 어댑터. */
class TermRepositoryImpl
    @Inject
    constructor(
        private val termDao: TermDao,
    ) : TermRepository {
        override fun observeTerms(collectionId: String): Flow<List<Term>> =
            termDao.observeByCollection(collectionId).map { list -> list.map { it.toDomain() } }

        override suspend fun upsert(term: Term): String {
            val id = term.id.ifBlank { UUID.randomUUID().toString() }
            termDao.upsert(term.copy(id = id, updatedAt = System.currentTimeMillis()).toEntity())
            return id
        }

        override suspend fun delete(id: String) {
            termDao.delete(id)
        }
    }
