package com.android.offread.library.data

import com.android.offread.core.database.ItemEntity
import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.LibraryItem

internal fun ItemEntity.toDomain(): LibraryItem =
    LibraryItem(
        id = id,
        collectionId = collectionId,
        type = ItemType.valueOf(type),
        title = title,
        author = author,
        sourceUrl = sourceUrl,
        siteName = siteName,
        totalChapters = totalChapters,
        serialStatus = SerialStatus.valueOf(serialStatus),
        translationStatus = TranslationStatus.valueOf(translationStatus),
        updatedAt = updatedAt,
        lastReadChapter = lastReadChapter,
    )

internal fun LibraryItem.toEntity(): ItemEntity =
    ItemEntity(
        id = id,
        collectionId = collectionId,
        type = type.name,
        title = title,
        author = author,
        sourceUrl = sourceUrl,
        siteName = siteName,
        totalChapters = totalChapters,
        serialStatus = serialStatus.name,
        translationStatus = translationStatus.name,
        updatedAt = updatedAt,
        lastReadChapter = lastReadChapter,
    )
