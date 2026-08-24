package com.android.offread.translate.data

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus
import com.android.offread.translate.domain.TermSuggestionSink
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * [TermSuggestionSink] 어댑터(F-024). 자동 제안은 SUGGESTED·AUTO 로 적어
 * 사용자가 용어맵(T-01)에서 수락하기 전까지 번역에 쓰이지 않게 한다.
 */
class TermRepositorySuggestionSink
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) : TermSuggestionSink {
        override suspend fun existingSources(collectionId: String): Set<String> =
            termRepository
                .observeTerms(collectionId)
                .first()
                .map { it.source }
                .toSet()

        override suspend fun suggest(
            collectionId: String,
            source: String,
            translation: String,
            occurrenceCount: Int,
        ) {
            termRepository.upsert(
                Term(
                    id = "",
                    collectionId = collectionId,
                    source = source,
                    translation = translation,
                    pinned = false,
                    origin = TermOrigin.AUTO,
                    occurrenceCount = occurrenceCount,
                    status = TermStatus.SUGGESTED,
                    updatedAt = 0,
                ),
            )
        }
    }
