package com.android.offread.translate.data

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.TermStatus
import com.android.offread.translate.domain.GlossaryProvider
import com.android.offread.translate.domain.model.GlossaryEntry
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * [GlossaryProvider] 어댑터. 컬렉션 용어맵에서 **확정된** 용어만 번역에 주입한다 —
 * 자동 제안(SUGGESTED)은 사용자가 수락하기 전까지 번역에 영향을 주지 않는다(F-024).
 */
class TermGlossaryProvider
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) : GlossaryProvider {
        override suspend fun glossaryFor(collectionId: String): List<GlossaryEntry> =
            termRepository
                .observeTerms(collectionId)
                .first()
                .filter { it.status == TermStatus.CONFIRMED }
                .map { GlossaryEntry(source = it.source, translation = it.translation, pinned = it.pinned) }
    }
