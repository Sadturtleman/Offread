package com.android.offread.terms.domain.usecase

import com.android.offread.terms.domain.TermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus
import javax.inject.Inject

/**
 * F-026: 용어 추가/수정. 원어·번역은 공백일 수 없다.
 * [existing] 이 있으면 원어·번역·고정만 갱신하고, 없으면 수동·확정 용어로 새로 만든다.
 */
class UpsertTermUseCase
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) {
        suspend operator fun invoke(input: Input): Result<String> {
            val source = input.source.trim()
            val translation = input.translation.trim()
            if (source.isEmpty() || translation.isEmpty()) {
                return Result.failure(BlankTermException)
            }
            val term =
                input.existing?.copy(
                    source = source,
                    translation = translation,
                    pinned = input.pinned,
                ) ?: Term(
                    id = "",
                    collectionId = input.collectionId,
                    source = source,
                    translation = translation,
                    pinned = input.pinned,
                    origin = TermOrigin.MANUAL,
                    occurrenceCount = 0,
                    status = TermStatus.CONFIRMED,
                    updatedAt = 0,
                )
            return Result.success(termRepository.upsert(term))
        }

        data class Input(
            val collectionId: String,
            val source: String,
            val translation: String,
            val pinned: Boolean,
            val existing: Term? = null,
        )
    }

/** F-026: 용어 삭제. */
class DeleteTermUseCase
    @Inject
    constructor(
        private val termRepository: TermRepository,
    ) {
        suspend operator fun invoke(id: String) = termRepository.delete(id)
    }

/** 원어 또는 번역이 공백일 때. */
object BlankTermException : IllegalArgumentException("원어와 번역을 모두 입력해 주세요.")
