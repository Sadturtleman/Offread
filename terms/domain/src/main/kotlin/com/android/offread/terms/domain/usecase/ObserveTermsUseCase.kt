package com.android.offread.terms.domain.usecase

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermFilter
import com.android.offread.terms.domain.model.TermOrigin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** F-025: 컬렉션 용어를 필터에 맞춰 구독한다. */
class ObserveTermsUseCase
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) {
        operator fun invoke(
            collectionId: String,
            filter: TermFilter,
        ): Flow<List<Term>> =
            termRepository.observeTerms(collectionId).map { terms ->
                when (filter) {
                    TermFilter.ALL -> terms
                    TermFilter.AUTO -> terms.filter { it.origin == TermOrigin.AUTO }
                    TermFilter.MANUAL -> terms.filter { it.origin == TermOrigin.MANUAL }
                    TermFilter.PINNED -> terms.filter { it.pinned }
                }
            }
    }
