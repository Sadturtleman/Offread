package com.android.offread.terms.domain.usecase

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermStatus
import javax.inject.Inject

/**
 * F-024: 자동 제안 용어를 수락한다(SUGGESTED → CONFIRMED).
 * (자동 추출 자체는 번역 파이프라인 붙을 때. 여기선 수락/거절 계약만 제공.)
 */
class AcceptSuggestionUseCase
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) {
        suspend operator fun invoke(term: Term) {
            termRepository.upsert(term.copy(status = TermStatus.CONFIRMED))
        }
    }

/** F-024: 자동 제안 용어를 거절한다(삭제). */
class RejectSuggestionUseCase
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) {
        suspend operator fun invoke(term: Term) = termRepository.delete(term.id)
    }
