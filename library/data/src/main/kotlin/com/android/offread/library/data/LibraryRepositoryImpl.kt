package com.android.offread.library.data

import com.android.offread.core.database.CollectionDao
import com.android.offread.core.database.CollectionEntity
import com.android.offread.core.database.ItemDao
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * [LibraryRepository] 의 Room 어댑터. 컬렉션 삭제 연쇄는 자기참조 FK(CASCADE)가 DB 레벨에서 처리한다.
 */
class LibraryRepositoryImpl
    @Inject
    constructor(
        private val collectionDao: CollectionDao,
        private val itemDao: ItemDao,
    ) : LibraryRepository {
        override fun observeCollections(sort: LibrarySort): Flow<List<Collection>> {
            val source =
                when (sort) {
                    LibrarySort.RECENT -> collectionDao.observeByRecent()
                    LibrarySort.NAME -> collectionDao.observeByName()
                }
            return source.map { list -> list.map(CollectionEntity::toDomain) }
        }

        override suspend fun createCollection(
            name: String,
            parentId: String?,
        ): String {
            val id = UUID.randomUUID().toString()
            collectionDao.insert(
                CollectionEntity(
                    id = id,
                    name = name,
                    parentId = parentId,
                    itemCount = 0,
                    termCount = 0,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            return id
        }

        override suspend fun renameCollection(
            id: String,
            name: String,
        ) {
            collectionDao.rename(id, name, System.currentTimeMillis())
        }

        override suspend fun deleteCollection(id: String) {
            collectionDao.delete(id)
        }

        override fun observeItems(collectionId: String?): Flow<List<LibraryItem>> {
            val source =
                if (collectionId == null) itemDao.observeAll() else itemDao.observeByCollection(collectionId)
            return source.map { list -> list.map { it.toDomain() } }
        }

        override fun observeItem(id: String): Flow<LibraryItem?> = itemDao.observeById(id).map { it?.toDomain() }

        override suspend fun addItem(item: LibraryItem): String {
            val id = item.id.ifBlank { UUID.randomUUID().toString() }
            itemDao.insert(item.copy(id = id, updatedAt = System.currentTimeMillis()).toEntity())
            return id
        }

        override suspend fun updateItemTranslationStatus(
            id: String,
            status: TranslationStatus,
        ) {
            itemDao.updateTranslationStatus(id, status.name, System.currentTimeMillis())
        }
    }
