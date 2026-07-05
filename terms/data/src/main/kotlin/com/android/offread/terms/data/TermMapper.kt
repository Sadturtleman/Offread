package com.android.offread.terms.data

import com.android.offread.core.database.TermEntity
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus

internal fun TermEntity.toDomain(): Term =
    Term(
        id = id,
        collectionId = collectionId,
        source = source,
        translation = translation,
        pinned = pinned,
        origin = TermOrigin.valueOf(origin),
        occurrenceCount = occurrenceCount,
        status = TermStatus.valueOf(status),
        updatedAt = updatedAt,
    )

internal fun Term.toEntity(): TermEntity =
    TermEntity(
        id = id,
        collectionId = collectionId,
        source = source,
        translation = translation,
        pinned = pinned,
        origin = origin.name,
        occurrenceCount = occurrenceCount,
        status = status.name,
        updatedAt = updatedAt,
    )
