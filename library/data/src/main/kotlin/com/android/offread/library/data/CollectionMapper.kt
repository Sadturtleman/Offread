package com.android.offread.library.data

import com.android.offread.core.database.CollectionEntity
import com.android.offread.library.domain.model.Collection

internal fun CollectionEntity.toDomain(): Collection =
    Collection(
        id = id,
        name = name,
        parentId = parentId,
        itemCount = itemCount,
        termCount = termCount,
        updatedAt = updatedAt,
    )
